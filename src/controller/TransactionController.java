package controller;

import db.DBConnection;
import model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionController {

    private Connection conn = DBConnection.getConnection();

    // Deposit করা
    public boolean deposit(String accountNumber, BigDecimal amount, String description) {
        try {
            conn.setAutoCommit(false); // Transaction শুরু

            // Balance update করা
            String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ? AND status = 'Active'";
            PreparedStatement ps1 = conn.prepareStatement(updateSql);
            ps1.setBigDecimal(1, amount);
            ps1.setString(2, accountNumber);
            int updated = ps1.executeUpdate();

            if (updated == 0) {
                conn.rollback();
                return false; // Account নেই বা inactive
            }

            // নতুন balance বের করা
            BigDecimal newBalance = getBalance(accountNumber);

            // Transaction record করা
            String insertSql = "INSERT INTO transactions (account_number, type, amount, balance_after, description) VALUES (?,?,?,?,?)";
            PreparedStatement ps2 = conn.prepareStatement(insertSql);
            ps2.setString(1, accountNumber);
            ps2.setString(2, "Deposit");
            ps2.setBigDecimal(3, amount);
            ps2.setBigDecimal(4, newBalance);
            ps2.setString(5, description);
            ps2.executeUpdate();

            conn.commit(); // সফল হলে save করা
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Withdrawal করা
    public String withdraw(String accountNumber, BigDecimal amount) {
        try {
            conn.setAutoCommit(false);

            // Balance check করা
            BigDecimal currentBalance = getBalance(accountNumber);
            if (currentBalance == null) return "Account পাওয়া যায়নি!";
            if (currentBalance.compareTo(amount) < 0) return "পর্যাপ্ত টাকা নেই! Balance: " + currentBalance;

            // Balance update করা
            String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ? AND status = 'Active'";
            PreparedStatement ps1 = conn.prepareStatement(updateSql);
            ps1.setBigDecimal(1, amount);
            ps1.setString(2, accountNumber);
            ps1.executeUpdate();

            BigDecimal newBalance = currentBalance.subtract(amount);

            // Transaction record করা
            String insertSql = "INSERT INTO transactions (account_number, type, amount, balance_after, description) VALUES (?,?,'Withdrawal',?,?,?)";
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO transactions (account_number, type, amount, balance_after, description) VALUES (?,?,?,?,?)");
            ps2.setString(1, accountNumber);
            ps2.setString(2, "Withdrawal");
            ps2.setBigDecimal(3, amount);
            ps2.setBigDecimal(4, newBalance);
            ps2.setString(5, "Cash withdrawal");
            ps2.executeUpdate();

            conn.commit();
            return "SUCCESS";

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Fund Transfer করা
    public String transfer(String fromAccount, String toAccount, BigDecimal amount) {
        try {
            conn.setAutoCommit(false);

            BigDecimal fromBalance = getBalance(fromAccount);
            if (fromBalance == null) return "Source account পাওয়া যায়নি!";
            if (fromBalance.compareTo(amount) < 0) return "পর্যাপ্ত টাকা নেই!";
            if (getBalance(toAccount) == null) return "Destination account পাওয়া যায়নি!";

            // From account থেকে কমানো
            PreparedStatement ps1 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
            ps1.setBigDecimal(1, amount);
            ps1.setString(2, fromAccount);
            ps1.executeUpdate();

            // To account-এ যোগ করা
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
            ps2.setBigDecimal(1, amount);
            ps2.setString(2, toAccount);
            ps2.executeUpdate();

            // দুটো transaction record করা
            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO transactions (account_number, type, amount, balance_after, description) VALUES (?,?,?,?,?)");

            ps3.setString(1, fromAccount);
            ps3.setString(2, "Transfer");
            ps3.setBigDecimal(3, amount);
            ps3.setBigDecimal(4, fromBalance.subtract(amount));
            ps3.setString(5, "Transfer to " + toAccount);
            ps3.executeUpdate();

            ps3.setString(1, toAccount);
            ps3.setString(2, "Transfer");
            ps3.setBigDecimal(3, amount);
            ps3.setBigDecimal(4, getBalance(toAccount));
            ps3.setString(5, "Transfer from " + fromAccount);
            ps3.executeUpdate();

            conn.commit();
            return "SUCCESS";

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return "Error: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Balance দেখা
    public BigDecimal getBalance(String accountNumber) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_number = ?");
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("balance");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Transaction history দেখা
    public List<Transaction> getHistory(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC LIMIT 50");
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setAccountNumber(rs.getString("account_number"));
                t.setType(rs.getString("type"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setBalanceAfter(rs.getBigDecimal("balance_after"));
                t.setDescription(rs.getString("description"));
                t.setTransactionDate(rs.getTimestamp("transaction_date"));
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // সব transaction দেখা
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM transactions ORDER BY transaction_date DESC");
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setAccountNumber(rs.getString("account_number"));
                t.setType(rs.getString("type"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setBalanceAfter(rs.getBigDecimal("balance_after"));
                t.setDescription(rs.getString("description"));
                t.setTransactionDate(rs.getTimestamp("transaction_date"));
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}