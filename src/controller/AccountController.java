package controller;

import db.DBConnection;
import model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AccountController {

    private Connection conn = DBConnection.getConnection();

    // Random account number generate করা
    private String generateAccountNumber() {
        Random rand = new Random();
        return "ACC" + (100000000 + rand.nextInt(900000000));
    }

    // নতুন account তৈরি করা
    public boolean createAccount(int customerId, String accountType, BigDecimal initialDeposit) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance) VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, generateAccountNumber());
            ps.setInt(2, customerId);
            ps.setString(3, accountType);
            ps.setBigDecimal(4, initialDeposit);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // সব accounts দেখা
    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id ORDER BY a.id DESC";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Account acc = new Account();
                acc.setId(rs.getInt("id"));
                acc.setAccountNumber(rs.getString("account_number"));
                acc.setCustomerId(rs.getInt("customer_id"));
                acc.setCustomerName(rs.getString("full_name"));
                acc.setAccountType(rs.getString("account_type"));
                acc.setBalance(rs.getBigDecimal("balance"));
                acc.setStatus(rs.getString("status"));
                acc.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Account number দিয়ে একটা account খোঁজা
    public Account findByAccountNumber(String accNum) {
        String sql = "SELECT a.*, c.full_name FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id WHERE a.account_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accNum);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Account acc = new Account();
                acc.setId(rs.getInt("id"));
                acc.setAccountNumber(rs.getString("account_number"));
                acc.setCustomerId(rs.getInt("customer_id"));
                acc.setCustomerName(rs.getString("full_name"));
                acc.setAccountType(rs.getString("account_type"));
                acc.setBalance(rs.getBigDecimal("balance"));
                acc.setStatus(rs.getString("status"));
                return acc;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean updateStatus(String accountNumber, String status) {
        String sql = "UPDATE accounts SET status = ? WHERE account_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, accountNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}