package controller;

import db.DBConnection;
import model.Loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanController {

    private Connection conn = DBConnection.getConnection();

    // Loan apply করা
    public boolean applyLoan(int customerId, String accountNumber,
                             BigDecimal amount, double interestRate, int durationMonths) {
        // Monthly payment calculate করা
        // Formula: M = P * [r(1+r)^n] / [(1+r)^n - 1]
        double r = (interestRate / 100) / 12;
        double n = durationMonths;
        double monthly;
        if (r == 0) {
            monthly = amount.doubleValue() / n;
        } else {
            monthly = amount.doubleValue() * (r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        }
        BigDecimal monthlyPayment = BigDecimal.valueOf(monthly).setScale(2, RoundingMode.HALF_UP);

        String sql = "INSERT INTO loans (customer_id, account_number, loan_amount, " +
                "interest_rate, duration_months, monthly_payment, status) VALUES (?,?,?,?,?,?,'Pending')";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            ps.setString(2, accountNumber);
            ps.setBigDecimal(3, amount);
            ps.setDouble(4, interestRate);
            ps.setInt(5, durationMonths);
            ps.setBigDecimal(6, monthlyPayment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // সব loan দেখা
    public List<Loan> getAllLoans() {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, c.full_name FROM loans l " +
                "JOIN customers c ON l.customer_id = c.id ORDER BY l.id DESC";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Loan loan = new Loan();
                loan.setId(rs.getInt("id"));
                loan.setCustomerId(rs.getInt("customer_id"));
                loan.setCustomerName(rs.getString("full_name"));
                loan.setAccountNumber(rs.getString("account_number"));
                loan.setLoanAmount(rs.getBigDecimal("loan_amount"));
                loan.setInterestRate(rs.getDouble("interest_rate"));
                loan.setDurationMonths(rs.getInt("duration_months"));
                loan.setMonthlyPayment(rs.getBigDecimal("monthly_payment"));
                loan.setAmountPaid(rs.getBigDecimal("amount_paid"));
                loan.setStatus(rs.getString("status"));
                list.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Loan approve করা
    public boolean approveLoan(int loanId) {
        try {
            // Loan info বের করা
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT * FROM loans WHERE id = ?");
            ps1.setInt(1, loanId);
            ResultSet rs = ps1.executeQuery();

            if (rs.next()) {
                String accountNumber = rs.getString("account_number");
                BigDecimal loanAmount = rs.getBigDecimal("loan_amount");

                conn.setAutoCommit(false);

                // Loan status update
                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE loans SET status='Approved', start_date=CURDATE() WHERE id=?");
                ps2.setInt(1, loanId);
                ps2.executeUpdate();

                // Account এ টাকা যোগ করা
                PreparedStatement ps3 = conn.prepareStatement(
                        "UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
                ps3.setBigDecimal(1, loanAmount);
                ps3.setString(2, accountNumber);
                ps3.executeUpdate();

                // Transaction record
                BigDecimal newBalance = getAccountBalance(accountNumber);
                PreparedStatement ps4 = conn.prepareStatement(
                        "INSERT INTO transactions (account_number, type, amount, balance_after, description) " +
                                "VALUES (?, 'Deposit', ?, ?, ?)");
                ps4.setString(1, accountNumber);
                ps4.setBigDecimal(2, loanAmount);
                ps4.setBigDecimal(3, newBalance);
                ps4.setString(4, "Loan approved - ID: " + loanId);
                ps4.executeUpdate();

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    // Loan reject করা
    public boolean rejectLoan(int loanId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE loans SET status='Rejected' WHERE id=? AND status='Pending'");
            ps.setInt(1, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Loan payment করা
    public String makeLoanPayment(int loanId, BigDecimal amount) {
        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT * FROM loans WHERE id=? AND status='Approved'");
            ps1.setInt(1, loanId);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) return "Loan পাওয়া যায়নি বা Approved নয়!";

            BigDecimal loanAmount  = rs.getBigDecimal("loan_amount");
            BigDecimal amountPaid  = rs.getBigDecimal("amount_paid");
            String accountNumber   = rs.getString("account_number");
            BigDecimal remaining   = loanAmount.subtract(amountPaid);

            if (amount.compareTo(remaining) > 0) {
                return "Payment amount বাকি loan এর চেয়ে বেশি! বাকি: TK " + remaining;
            }

            // Account থেকে টাকা কাটা
            BigDecimal balance = getAccountBalance(accountNumber);
            if (balance == null || balance.compareTo(amount) < 0) {
                return "Account এ পর্যাপ্ত টাকা নেই! Balance: TK " + balance;
            }

            conn.setAutoCommit(false);

            // Account balance কমানো
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
            ps2.setBigDecimal(1, amount);
            ps2.setString(2, accountNumber);
            ps2.executeUpdate();

            // Loan amount_paid বাড়ানো
            BigDecimal newAmountPaid = amountPaid.add(amount);
            String newStatus = newAmountPaid.compareTo(loanAmount) >= 0 ? "Closed" : "Approved";

            PreparedStatement ps3 = conn.prepareStatement(
                    "UPDATE loans SET amount_paid=?, status=? WHERE id=?");
            ps3.setBigDecimal(1, newAmountPaid);
            ps3.setString(2, newStatus);
            ps3.setInt(3, loanId);
            ps3.executeUpdate();

            // Payment record
            PreparedStatement ps4 = conn.prepareStatement(
                    "INSERT INTO loan_payments (loan_id, amount) VALUES (?,?)");
            ps4.setInt(1, loanId);
            ps4.setBigDecimal(2, amount);
            ps4.executeUpdate();

            // Transaction record
            BigDecimal newBalance = balance.subtract(amount);
            PreparedStatement ps5 = conn.prepareStatement(
                    "INSERT INTO transactions (account_number, type, amount, balance_after, description) " +
                            "VALUES (?, 'Withdrawal', ?, ?, ?)");
            ps5.setString(1, accountNumber);
            ps5.setBigDecimal(2, amount);
            ps5.setBigDecimal(3, newBalance);
            ps5.setString(4, "Loan payment - ID: " + loanId);
            ps5.executeUpdate();

            conn.commit();
            return "SUCCESS:" + newStatus;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private BigDecimal getAccountBalance(String accountNumber) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_number=?");
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("balance");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}