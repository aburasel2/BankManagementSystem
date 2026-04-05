package view;

import controller.AdminController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private AdminController adminController;

    public LoginFrame() {
        adminController = new AdminController();
        initUI();
    }

    private void initUI() {
        setTitle("Bank Management System - Login");
        setSize(900, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // ── Left Panel (Blue side) ──
        JPanel leftPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(15, 40, 80));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(420, 580));
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 20, 10, 20);

        // Bank icon
        JLabel iconLabel = new JLabel("🏦");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 72));
        gbc.gridy = 0;
        leftPanel.add(iconLabel, gbc);

        // Bank name
        JLabel bankName = new JLabel("Bangladesh Bank");
        bankName.setFont(new Font("Arial", Font.BOLD, 26));
        bankName.setForeground(Color.WHITE);
        gbc.gridy = 1;
        leftPanel.add(bankName, gbc);

        // Subtitle
        JLabel subTitle = new JLabel("Management System");
        subTitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subTitle.setForeground(new Color(150, 190, 240));
        gbc.gridy = 2;
        leftPanel.add(subTitle, gbc);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(280, 1));
        sep.setForeground(new Color(60, 100, 160));
        gbc.gridy = 3;
        leftPanel.add(sep, gbc);

        // Features list
        String[] features = {
                "✔  Customer Management",
                "✔  Account Management",
                "✔  Deposit & Withdrawal",
                "✔  Fund Transfer",
                "✔  Loan Management",
                "✔  Transaction History"
        };
        for (int i = 0; i < features.length; i++) {
            JLabel feat = new JLabel(features[i]);
            feat.setFont(new Font("Arial", Font.PLAIN, 13));
            feat.setForeground(new Color(170, 210, 255));
            gbc.gridy = 4 + i;
            gbc.anchor = GridBagConstraints.WEST;
            leftPanel.add(feat, gbc);
        }

        // Version
        JLabel version = new JLabel("v1.0.0  ©  2026");
        version.setFont(new Font("Arial", Font.PLAIN, 11));
        version.setForeground(new Color(100, 140, 190));
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.CENTER;
        leftPanel.add(version, gbc);

        // ── Right Panel (Login form) ──
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(245, 247, 252));
        rightPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.insets = new Insets(8, 0, 8, 0);

        // Login title
        JLabel loginTitle = new JLabel("Admin Login");
        loginTitle.setFont(new Font("Arial", Font.BOLD, 28));
        loginTitle.setForeground(new Color(15, 40, 80));
        r.gridy = 0;
        rightPanel.add(loginTitle, r);

        JLabel loginSub = new JLabel("Please enter your credentials");
        loginSub.setFont(new Font("Arial", Font.PLAIN, 13));
        loginSub.setForeground(new Color(130, 140, 160));
        r.gridy = 1;
        rightPanel.add(loginSub, r);

        // Spacer
        r.gridy = 2;
        rightPanel.add(Box.createVerticalStrut(10), r);

        // Username label
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));
        userLabel.setForeground(new Color(50, 70, 110));
        r.gridy = 3;
        rightPanel.add(userLabel, r);

        // Username field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(320, 42));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1, true),
                new EmptyBorder(5, 12, 5, 12)));
        usernameField.setBackground(Color.WHITE);
        r.gridy = 4;
        rightPanel.add(usernameField, r);

        // Password label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.BOLD, 13));
        passLabel.setForeground(new Color(50, 70, 110));
        r.gridy = 5;
        rightPanel.add(passLabel, r);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(320, 42));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1, true),
                new EmptyBorder(5, 12, 5, 12)));
        passwordField.setBackground(Color.WHITE);
        r.gridy = 6;
        rightPanel.add(passwordField, r);

        // Spacer
        r.gridy = 7;
        rightPanel.add(Box.createVerticalStrut(5), r);

        // Login button
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 15));
        loginBtn.setBackground(new Color(0, 100, 200));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(320, 46));
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginBtn.setBackground(new Color(0, 80, 170));
            }
            public void mouseExited(MouseEvent e) {
                loginBtn.setBackground(new Color(0, 100, 200));
            }
        });
        r.gridy = 8;
        rightPanel.add(loginBtn, r);

        // Default credentials hint
        JLabel hint = new JLabel("Default: admin / admin123");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(new Color(160, 170, 190));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        r.gridy = 9;
        rightPanel.add(hint, r);

        // ── Actions ──
        loginBtn.addActionListener(e -> performLogin());
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });

        // ── Assemble ──
        add(leftPanel,  BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username এবং Password দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (adminController.login(username, password)) {
            new DashboardFrame(username);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username বা Password ভুল!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}