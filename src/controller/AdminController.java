package controller;

import db.DBConnection;
import model.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    private Connection conn = DBConnection.getConnection();
    private Admin loggedInAdmin = null;

    // Login — role সহ return করে
    public Admin loginWithRole(String username, String password) {
        String sql = "SELECT * FROM admin_users WHERE username=? AND password=? AND status='Active'";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setUsername(rs.getString("username"));
                admin.setFullName(rs.getString("full_name"));
                admin.setRole(rs.getString("role"));
                admin.setStatus(rs.getString("status"));

                // Last login update
                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE admin_users SET last_login=NOW() WHERE id=?");
                ps2.setInt(1, admin.getId());
                ps2.executeUpdate();

                loggedInAdmin = admin;
                return admin;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // পুরনো login method — backward compatibility
    public boolean login(String username, String password) {
        return loginWithRole(username, password) != null;
    }

    // সব admin দেখা
    public List<Admin> getAllAdmins() {
        List<Admin> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM admin_users ORDER BY id");
            while (rs.next()) {
                Admin a = new Admin();
                a.setId(rs.getInt("id"));
                a.setUsername(rs.getString("username"));
                a.setFullName(rs.getString("full_name"));
                a.setRole(rs.getString("role"));
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // নতুন admin যোগ করা
    public boolean addAdmin(Admin admin) {
        String sql = "INSERT INTO admin_users (username, password, full_name, role) VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPassword());
            ps.setString(3, admin.getFullName());
            ps.setString(4, admin.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Admin status update (Active/Inactive)
    public boolean updateStatus(int adminId, String status) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE admin_users SET status=? WHERE id=?");
            ps.setString(1, status);
            ps.setInt(2, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Password change করা
    public boolean changePassword(int adminId, String oldPass, String newPass) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE admin_users SET password=? WHERE id=? AND password=?");
            ps.setString(1, newPass);
            ps.setInt(2, adminId);
            ps.setString(3, oldPass);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Admin getLoggedInAdmin() { return loggedInAdmin; }
}