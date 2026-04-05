package controller;

import db.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterestController {

    private Connection conn = DBConnection.getConnection();

    // Interest rate পাওয়া
    public Map<String, Double> getInterestRates() {
        Map<String, Double> rates = new HashMap<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM interest_settings");
            while (rs.next()) {
                rates.put(rs.getString("account_type"), rs.getDouble("annual_rate"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rates;
    }

    // Interest rate update করা
    public boolean updateRate(String accountType, double rate) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE interest_settings SET annual_rate=? WHERE account_type=?");
            ps.setDouble(1, rate);
            ps.setString(2, accountType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // সব Savings/Fixed account এ interest apply করা
    public String applyMonthlyInterest() {
        int successCount = 0;
        int skipCount = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        try {
            // Interest rates বের করা
            Map<String, Double> rates = getInterestRates();

            // সব active account বের করা
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM accounts WHERE status='Active' AND account_type != 'Current'");

            while (rs.next()) {
                String accNum     = rs.getString("account_number");
                String accType    = rs.getString("account_type");
                BigDecimal balance = rs.getBigDecimal("balance");

                // এই মাসে interest দেওয়া হয়েছে কিনা check করা
                PreparedStatement checkPs = conn.prepareStatement(
                        "SELECT COUNT(*) FROM interest_history " +
                                "WHERE account_number=? AND MONTH(applied_date)=MONTH(NOW()) " +
                                "AND YEAR(applied_date)=YEAR(NOW())");
                checkPs.setString(1, accNum);
                ResultSet checkRs = checkPs.executeQuery();
                checkRs.next();
                if (checkRs.getInt(1) > 0) {
                    skipCount++;
                    continue; // এই মাসে already দেওয়া হয়েছে
                }

                // Monthly interest calculate করা
                double annualRate = rates.getOrDefault(accType, 5.0);
                double monthlyRate = annualRate / 12 / 100;
                BigDecimal interest = balance.multiply(
                        BigDecimal.valueOf(monthlyRate)).setScale(2, RoundingMode.HALF_UP);

                if (interest.compareTo(BigDecimal.ZERO) <= 0) continue;

                conn.setAutoCommit(false);

                // Balance update
                BigDecimal newBalance = balance.add(interest);
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE account_number=?");
                ps1.setBigDecimal(1, newBalance);
                ps1.setString(2, accNum);
                ps1.executeUpdate();

                // Interest history record
                PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO interest_history " +
                                "(account_number, interest_amount, balance_before, balance_after, rate) " +
                                "VALUES (?,?,?,?,?)");
                ps2.setString(1, accNum);
                ps2.setBigDecimal(2, interest);
                ps2.setBigDecimal(3, balance);
                ps2.setBigDecimal(4, newBalance);
                ps2.setDouble(5, annualRate);
                ps2.executeUpdate();

                // Transaction record
                PreparedStatement ps3 = conn.prepareStatement(
                        "INSERT INTO transactions " +
                                "(account_number, type, amount, balance_after, description) " +
                                "VALUES (?, 'Deposit', ?, ?, ?)");
                ps3.setString(1, accNum);
                ps3.setBigDecimal(2, interest);
                ps3.setBigDecimal(3, newBalance);
                ps3.setString(4, String.format("Monthly interest (%.2f%% p.a.)", annualRate));
                ps3.executeUpdate();

                conn.commit();
                totalInterest = totalInterest.add(interest);
                successCount++;
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }

        return String.format("SUCCESS:%d:%d:%.2f", successCount, skipCount,
                totalInterest.doubleValue());
    }

    // Interest history দেখা
    public List<Map<String, Object>> getInterestHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT ih.*, a.account_type FROM interest_history ih " +
                            "JOIN accounts a ON ih.account_number = a.account_number " +
                            "ORDER BY ih.applied_date DESC LIMIT 100");
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id",              rs.getInt("id"));
                row.put("account_number",  rs.getString("account_number"));
                row.put("account_type",    rs.getString("account_type"));
                row.put("interest_amount", rs.getBigDecimal("interest_amount"));
                row.put("balance_before",  rs.getBigDecimal("balance_before"));
                row.put("balance_after",   rs.getBigDecimal("balance_after"));
                row.put("rate",            rs.getDouble("rate"));
                row.put("applied_date",    rs.getTimestamp("applied_date"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}