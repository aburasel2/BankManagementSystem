package controller;

import db.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ATMController {

    private Connection conn = DBConnection.getConnection();

    // 16 digit card number generate
    private String generateCardNumber() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder("4"); // Visa prefix
        for (int i = 0; i < 15; i++) sb.append(rand.nextInt(10));
        return sb.toString();
    }

    // Expiry date — 3 বছর পরে
    private String generateExpiryDate() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, 3);
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    // Card format — XXXX XXXX XXXX XXXX
    public String formatCardNumber(String cardNum) {
        if (cardNum == null || cardNum.length() != 16) return cardNum;
        return cardNum.substring(0, 4) + " " + cardNum.substring(4, 8) + " " +
                cardNum.substring(8, 12) + " " + cardNum.substring(12, 16);
    }

    // নতুন card issue করা
    public String issueCard(int customerId, String accountNumber,
                            String cardType, String pin, BigDecimal dailyLimit) {
        try {
            // Account exist করে কিনা check
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT * FROM accounts WHERE account_number=? AND status='Active'");
            ps1.setString(1, accountNumber);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) return "Account পাওয়া যায়নি বা inactive!";

            // Already card আছে কিনা check
            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT * FROM atm_cards WHERE account_number=? AND status != 'Expired'");
            ps2.setString(1, accountNumber);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) return "এই account এ already একটা active card আছে!";

            String cardNumber  = generateCardNumber();
            String expiryDate  = generateExpiryDate();

            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO atm_cards " +
                            "(card_number, account_number, customer_id, card_type, " +
                            "pin_hash, expiry_date, daily_limit) VALUES (?,?,?,?,?,?,?)");
            ps3.setString(1, cardNumber);
            ps3.setString(2, accountNumber);
            ps3.setInt(3, customerId);
            ps3.setString(4, cardType);
            ps3.setString(5, pin);
            ps3.setString(6, expiryDate);
            ps3.setBigDecimal(7, dailyLimit);
            ps3.executeUpdate();

            return "SUCCESS:" + cardNumber;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // সব cards দেখা
    public List<Map<String, Object>> getAllCards() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT a.*, c.full_name FROM atm_cards a " +
                            "JOIN customers c ON a.customer_id = c.id " +
                            "ORDER BY a.id DESC");
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id",             rs.getInt("id"));
                row.put("card_number",    rs.getString("card_number"));
                row.put("customer_name",  rs.getString("full_name"));
                row.put("account_number", rs.getString("account_number"));
                row.put("card_type",      rs.getString("card_type"));
                row.put("expiry_date",    rs.getDate("expiry_date"));
                row.put("daily_limit",    rs.getBigDecimal("daily_limit"));
                row.put("status",         rs.getString("status"));
                row.put("issued_date",    rs.getDate("issued_date"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Card block/unblock করা
    public boolean updateCardStatus(int cardId, String status) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE atm_cards SET status=? WHERE id=?");
            ps.setString(1, status);
            ps.setInt(2, cardId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // PIN change করা
    public String changePin(int cardId, String oldPin, String newPin) {
        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT * FROM atm_cards WHERE id=? AND pin_hash=?");
            ps1.setInt(1, cardId);
            ps1.setString(2, oldPin);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) return "পুরনো PIN ভুল!";

            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE atm_cards SET pin_hash=? WHERE id=?");
            ps2.setString(1, newPin);
            ps2.setInt(2, cardId);
            ps2.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // Daily limit update করা
    public boolean updateDailyLimit(int cardId, BigDecimal limit) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE atm_cards SET daily_limit=? WHERE id=?");
            ps.setBigDecimal(1, limit);
            ps.setInt(2, cardId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Expired cards update করা
    public void checkAndExpireCards() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE atm_cards SET status='Expired' " +
                            "WHERE status='Active' AND expiry_date < CURDATE()");
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}