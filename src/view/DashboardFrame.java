package view;

import model.Admin;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class DashboardFrame extends JFrame {

    private Admin loggedInAdmin;
    private JPanel contentPanel;
    private JButton activeBtn = null;

    // Admin object দিয়ে constructor
    public DashboardFrame(Admin admin) {
        this.loggedInAdmin = admin;
        initUI();
    }

    // পুরনো String constructor — backward compatibility
    public DashboardFrame(String username) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setFullName(username);
        admin.setRole("Super Admin");
        this.loggedInAdmin = admin;
        initUI();
    }

    private void initUI() {
        setTitle("Bank Management System - " + loggedInAdmin.getRole());
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ── Top Bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 40, 80));
        topBar.setPreferredSize(new Dimension(1100, 65));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Left: Logo + Title
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTop.setOpaque(false);
        JLabel logo = new JLabel("🏦");
        logo.setFont(new Font("Arial", Font.PLAIN, 28));
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel bankName = new JLabel("Bangladesh Bank");
        bankName.setFont(new Font("Arial", Font.BOLD, 18));
        bankName.setForeground(Color.WHITE);
        JLabel bankSub = new JLabel("Management System");
        bankSub.setFont(new Font("Arial", Font.PLAIN, 11));
        bankSub.setForeground(new Color(180, 200, 230));
        titlePanel.add(bankName);
        titlePanel.add(bankSub);
        leftTop.add(logo);
        leftTop.add(titlePanel);

        // Right: User info + Logout
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);

        // Role badge
        JLabel roleBadge = new JLabel("  " + loggedInAdmin.getRole() + "  ");
        roleBadge.setFont(new Font("Arial", Font.BOLD, 11));
        roleBadge.setOpaque(true);
        switch (loggedInAdmin.getRole()) {
            case "Super Admin": roleBadge.setBackground(new Color(180, 50, 50));  break;
            case "Manager":     roleBadge.setBackground(new Color(30, 130, 80));  break;
            default:            roleBadge.setBackground(new Color(30, 100, 180)); break;
        }
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel userPanel = new JPanel(new GridLayout(2, 1));
        userPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        welcomeLabel.setForeground(new Color(180, 200, 230));
        JLabel userNameLabel = new JLabel(loggedInAdmin.getFullName().toUpperCase());
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userNameLabel.setForeground(new Color(100, 220, 180));
        userPanel.add(welcomeLabel);
        userPanel.add(userNameLabel);

        // Admin Management button — শুধু Super Admin দেখবে
        if ("Super Admin".equals(loggedInAdmin.getRole())) {
            JButton adminBtn = new JButton("👥 Admins");
            adminBtn.setFont(new Font("Arial", Font.BOLD, 12));
            adminBtn.setBackground(new Color(60, 100, 160));
            adminBtn.setForeground(Color.WHITE);
            adminBtn.setBorderPainted(false);
            adminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            adminBtn.setPreferredSize(new Dimension(100, 32));
            adminBtn.addActionListener(e -> new AdminManagementFrame(loggedInAdmin));
            rightTop.add(adminBtn);
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(200, 60, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(80, 32));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Logout করবে?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame();
            }
        });

        rightTop.add(roleBadge);
        rightTop.add(userPanel);
        rightTop.add(logoutBtn);

        topBar.add(leftTop,  BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);

        // ── Side Menu ──
        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBackground(new Color(20, 50, 100));
        sideMenu.setPreferredSize(new Dimension(210, 680));
        sideMenu.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel menuLabel = new JLabel("  MAIN MENU");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 11));
        menuLabel.setForeground(new Color(120, 160, 210));
        menuLabel.setBorder(new EmptyBorder(10, 15, 10, 0));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideMenu.add(menuLabel);

        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(245, 247, 252));

        String role = loggedInAdmin.getRole();

        // সবাই দেখতে পাবে
        addMenuItem(sideMenu, "👤", "Customers",        "Customers",        new CustomerPanel());
        addMenuItem(sideMenu, "🏦", "Accounts",         "Accounts",         new AccountPanel());
        addMenuItem(sideMenu, "💰", "Deposit/Withdraw", "Deposit/Withdraw", new TransactionPanel());
        addMenuItem(sideMenu, "🔄", "Fund Transfer",    "Fund Transfer",    new FundTransferPanel());
        addMenuItem(sideMenu, "📋", "Transactions",     "Transactions",     new TransactionHistoryPanel());
        addMenuItem(sideMenu, "💳", "ATM Cards",     "ATM Cards",     new ATMPanel());
        addMenuItem(sideMenu, "📄", "Statement", "Statement", new StatementPanel());
        addMenuItem(sideMenu, "💳", "ATM Cards", "ATM Cards", new ATMPanel());
        addMenuItem(sideMenu, "📧", "Notifications", "Notifications", new NotificationPanel());
        // Manager + Super Admin দেখতে পাবে
        if ("Super Admin".equals(role) || "Manager".equals(role)) {
            addMenuItem(sideMenu, "💳", "Loans",    "Loans",    new LoanPanel());
            addMenuItem(sideMenu, "🏛", "FDR",      "FDR",      new FDRPanel());
            addMenuItem(sideMenu, "💹", "Interest", "Interest", new InterestPanel());
        }

        // Bottom
        sideMenu.add(Box.createVerticalGlue());
        JLabel version = new JLabel("  v1.0.0  ©  2026");
        version.setFont(new Font("Arial", Font.PLAIN, 10));
        version.setForeground(new Color(100, 130, 170));
        version.setBorder(new EmptyBorder(10, 15, 5, 0));
        sideMenu.add(version);

        // ── Status Bar ──
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(15, 40, 80));
        statusBar.setPreferredSize(new Dimension(1100, 28));
        statusBar.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel statusLeft = new JLabel("● Connected  |  " +
                loggedInAdmin.getFullName() + "  |  Role: " + loggedInAdmin.getRole());
        statusLeft.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLeft.setForeground(new Color(100, 220, 150));

        JLabel statusRight = new JLabel(
                new java.text.SimpleDateFormat("dd MMM yyyy  HH:mm").format(new java.util.Date()));
        statusRight.setFont(new Font("Arial", Font.PLAIN, 11));
        statusRight.setForeground(new Color(180, 200, 230));

        statusBar.add(statusLeft,  BorderLayout.WEST);
        statusBar.add(statusRight, BorderLayout.EAST);

        // ── Assemble ──
        add(topBar,        BorderLayout.NORTH);
        add(sideMenu,      BorderLayout.WEST);
        add(contentPanel,  BorderLayout.CENTER);
        add(statusBar,     BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addMenuItem(JPanel sideMenu, String icon, String label,
                             String panelName, JPanel panel) {
        contentPanel.add(panel, panelName);
        JButton btn = createMenuButton(icon, label, panelName);
        sideMenu.add(btn);
        sideMenu.add(Box.createVerticalStrut(3));
        if (activeBtn == null) {
            setActiveButton(btn);
            activeBtn = btn;
        }
    }

    private JButton createMenuButton(String icon, String label, String panelName) {
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setForeground(new Color(200, 215, 240));
        btn.setBackground(new Color(20, 50, 100));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 10));
        btn.setMaximumSize(new Dimension(210, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(new Color(35, 70, 130));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(new Color(20, 50, 100));
            }
        });

        btn.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, panelName);
            if (activeBtn != null) resetButton(activeBtn);
            setActiveButton(btn);
            activeBtn = btn;
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        btn.setBackground(new Color(0, 120, 200));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void resetButton(JButton btn) {
        btn.setBackground(new Color(20, 50, 100));
        btn.setForeground(new Color(200, 215, 240));
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
    }
}