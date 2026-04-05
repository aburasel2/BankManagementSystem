package controller;

import db.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FDRController {

    private Connection conn = DBConnection.getConnection();

    // FDR number generate করা
    private String generateFDRNumber() {
        return "FDR" + (100000000 + new Random().nextInt(900000000));
    }

    // Maturity amount calculate করা
    // Formula: A = P * (1 + r/n)^(n*t)
    public BigDecimal calculateMaturity(BigDecimal principal, double annualRate, int months) {
        double r = annualRate / 100;
        double t = months / 12.0;
        double maturity = principal.doubleValue() * Math.pow(1 + r, t);
        return BigDecimal.valueOf(maturity).setScale(2, RoundingMode.HALF_UP);
    }

    // FDR তৈরি করা
    public String createFDR(int customerId, String accountNumber,
                            BigDecimal amount, double rate, int months) {
        try {
            conn.setAutoCommit(false);

            // Account এ পর্যাপ্ত balance আছে কিনা check
            PreparedStatement balPs = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_number=? AND status='Active'");
            balPs.setString(1, accountNumber);
            ResultSet balRs = balPs.executeQuery();
            if (!balRs.next()) {
                conn.rollback();
                return "Account পাওয়া যায়নি বা inactive!";
            }
            BigDecimal balance = balRs.getBigDecimal("balance");
            if (balance.compareTo(amount) < 0) {
                conn.rollback();
                return "পর্যাপ্ত balance নেই! Balance: TK " + balance;
            }

            // Maturity calculate করা
            BigDecimal maturityAmount = calculateMaturity(amount, rate, months);
            String fdrNumber = generateFDRNumber();

            // Account থেকে টাকা কাটা
            PreparedStatement ps1 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_number=?");
            ps1.setBigDecimal(1, amount);
            ps1.setString(2, accountNumber);
            ps1.executeUpdate();

            // FDR তৈরি করা
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO fixed_deposits " +
                            "(customer_id, account_number, fdr_number, principal_amount, " +
                            "interest_rate, duration_months, maturity_amount, start_date, maturity_date) " +
                            "VALUES (?,?,?,?,?,?,?,CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? MONTH))");
            ps2.setInt(1, customerId);
            ps2.setString(2, accountNumber);
            ps2.setString(3, fdrNumber);
            ps2.setBigDecimal(4, amount);
            ps2.setDouble(5, rate);
            ps2.setInt(6, months);
            ps2.setBigDecimal(7, maturityAmount);
            ps2.setInt(8, months);
            ps2.executeUpdate();

            // Transaction record
            BigDecimal newBalance = balance.subtract(amount);
            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO transactions " +
                            "(account_number, type, amount, balance_after, description) " +
                            "VALUES (?, 'Withdrawal', ?, ?, ?)");
            ps3.setString(1, accountNumber);
            ps3.setBigDecimal(2, amount);
            ps3.setBigDecimal(3, newBalance);
            ps3.setString(4, "FDR Created - " + fdrNumber);
            ps3.executeUpdate();

            conn.commit();
            return "SUCCESS:" + fdrNumber;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // সব FDR দেখা
    public List<Map<String, Object>> getAllFDRs() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT f.*, c.full_name FROM fixed_deposits f " +
                            "JOIN customers c ON f.customer_id = c.id " +
                            "ORDER BY f.id DESC");
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id",               rs.getInt("id"));
                row.put("fdr_number",       rs.getString("fdr_number"));
                row.put("customer_name",    rs.getString("full_name"));
                row.put("account_number",   rs.getString("account_number"));
                row.put("principal_amount", rs.getBigDecimal("principal_amount"));
                row.put("interest_rate",    rs.getDouble("interest_rate"));
                row.put("duration_months",  rs.getInt("duration_months"));
                row.put("maturity_amount",  rs.getBigDecimal("maturity_amount"));
                row.put("start_date",       rs.getDate("start_date"));
                row.put("maturity_date",    rs.getDate("maturity_date"));
                row.put("status",           rs.getString("status"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // FDR encash করা (maturity date এর পরে)
    public String encashFDR(int fdrId) {
        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT * FROM fixed_deposits WHERE id=? AND status='Active'");
            ps1.setInt(1, fdrId);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return "FDR পাওয়া যায়নি বা already closed!";
            }

            String accNumber      = rs.getString("account_number");
            BigDecimal maturity   = rs.getBigDecimal("maturity_amount");
            String fdrNumber      = rs.getString("fdr_number");
            Date maturityDate     = rs.getDate("maturity_date");
            Date today            = new Date(System.currentTimeMillis());

            // Maturity date check
            if (today.before(maturityDate)) {
                // Early withdrawal — শুধু principal ফেরত
                BigDecimal principal = rs.getBigDecimal("principal_amount");
                maturity = principal;
            }

            // Account এ টাকা যোগ করা
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_number=?");
            ps2.setBigDecimal(1, maturity);
            ps2.setString(2, accNumber);
            ps2.executeUpdate();

            // FDR status update
            PreparedStatement ps3 = conn.prepareStatement(
                    "UPDATE fixed_deposits SET status='Closed' WHERE id=?");
            ps3.setInt(1, fdrId);
            ps3.executeUpdate();

            // Balance বের করা
            PreparedStatement balPs = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_number=?");
            balPs.setString(1, accNumber);
            ResultSet balRs = balPs.executeQuery();
            balRs.next();
            BigDecimal newBalance = balRs.getBigDecimal("balance");

            // Transaction record
            PreparedStatement ps4 = conn.prepareStatement(
                    "INSERT INTO transactions " +
                            "(account_number, type, amount, balance_after, description) " +
                            "VALUES (?, 'Deposit', ?, ?, ?)");
            ps4.setString(1, accNumber);
            ps4.setBigDecimal(2, maturity);
            ps4.setBigDecimal(3, newBalance);
            ps4.setString(4, "FDR Encashed - " + fdrNumber);
            ps4.executeUpdate();

            conn.commit();
            return "SUCCESS:TK " + maturity;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Matured FDR গুলো automatically update করা
    public int checkAndUpdateMaturedFDRs() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE fixed_deposits SET status='Matured' " +
                            "WHERE status='Active' AND maturity_date <= CURDATE()");
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}