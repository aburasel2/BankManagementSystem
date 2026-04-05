package view;

import controller.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NotificationPanel extends JPanel {

    private JTextField toEmailField, subjectField;
    private JTextArea bodyArea;
    private JLabel statusLabel;

    public NotificationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Email Notification");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(15, 40, 80));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("Send Email",     buildSendPanel());
        tabs.addTab("Quick Templates", buildTemplatePanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Custom Email ──
    private JPanel buildSendPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Email Details"));

        formPanel.add(new JLabel("To Email:"));
        toEmailField = new JTextField();
        toEmailField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(toEmailField);

        formPanel.add(new JLabel("Subject:"));
        subjectField = new JTextField("Bangladesh Bank Notification");
        subjectField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(subjectField);

        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        // Body
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 5));
        bodyPanel.setBackground(Color.WHITE);
        JLabel bodyLabel = new JLabel("Message:");
        bodyLabel.setFont(new Font("Arial", Font.BOLD, 13));
        bodyArea = new JTextArea(8, 20);
        bodyArea.setFont(new Font("Arial", Font.PLAIN, 13));
        bodyArea.setLineWrap(true);
        bodyArea.setText("Dear Customer,\n\nআপনার account এ একটি transaction সম্পন্ন হয়েছে।\n\nBangladesh Bank");
        bodyPanel.add(bodyLabel, BorderLayout.NORTH);
        bodyPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        // Buttons + Status
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(Color.WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton sendBtn  = createButton("Send Email", new Color(0, 150, 100));
        JButton clearBtn = createButton("Clear",      new Color(120, 120, 120));
        btnPanel.add(sendBtn);
        btnPanel.add(clearBtn);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));

        bottomPanel.add(btnPanel,     BorderLayout.WEST);
        bottomPanel.add(statusLabel,  BorderLayout.CENTER);

        panel.add(formPanel,   BorderLayout.NORTH);
        panel.add(bodyPanel,   BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        sendBtn.addActionListener(e -> sendCustomEmail());
        clearBtn.addActionListener(e -> {
            toEmailField.setText("");
            subjectField.setText("Bangladesh Bank Notification");
            bodyArea.setText("Dear Customer,\n\nআপনার account এ একটি transaction সম্পন্ন হয়েছে।\n\nBangladesh Bank");
            statusLabel.setText(" ");
        });

        return panel;
    }

    // ── Tab 2: Quick Templates ──
    private JPanel buildTemplatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel sub = new JLabel("Quick test email পাঠাও");
        sub.setFont(new Font("Arial", Font.BOLD, 15));
        sub.setForeground(new Color(15, 40, 80));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panel.add(sub, gbc);

        // Test email field
        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Test Email:"), gbc);
        JTextField testEmailField = new JTextField();
        testEmailField.setFont(new Font("Arial", Font.PLAIN, 14));
        testEmailField.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(testEmailField, gbc);

        // Template buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Templates:"), gbc);

        JButton welcomeBtn    = createButton("Welcome Email",    new Color(0, 150, 100));
        JButton depositBtn    = createButton("Deposit Alert",    new Color(30, 100, 200));
        JButton withdrawBtn   = createButton("Withdraw Alert",   new Color(200, 50, 50));
        JButton loanBtn       = createButton("Loan Approval",    new Color(100, 50, 180));

        JPanel templateBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        templateBtns.setBackground(Color.WHITE);
        templateBtns.add(welcomeBtn);
        templateBtns.add(depositBtn);
        templateBtns.add(withdrawBtn);
        templateBtns.add(loanBtn);

        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(templateBtns, gbc);

        // Status
        JLabel templateStatus = new JLabel(" ");
        templateStatus.setFont(new Font("Arial", Font.BOLD, 13));
        templateStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        panel.add(templateStatus, gbc);

        // Info
        JLabel info = new JLabel(
                "<html><center><i>EmailService.java তে তোমার Gmail আর App Password দিতে হবে।</i></center></html>");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        info.setForeground(new Color(130, 140, 160));
        gbc.gridy = 4;
        panel.add(info, gbc);

        // Actions
        welcomeBtn.addActionListener(e -> {
            String email = testEmailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email দিন!");
                return;
            }
            templateStatus.setForeground(new Color(30, 100, 200));
            templateStatus.setText("Sending...");
            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                protected Boolean doInBackground() {
                    return EmailService.sendWelcomeEmail(
                            email, "Test Customer", "ACC123456789", "Savings");
                }
                protected void done() {
                    try {
                        boolean ok = get();
                        templateStatus.setForeground(ok ?
                                new Color(0, 130, 80) : new Color(200, 50, 50));
                        templateStatus.setText(ok ?
                                "✔ Welcome email পাঠানো হয়েছে!" :
                                "✘ Failed! Gmail App Password check করো।");
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            };
            w.execute();
        });

        depositBtn.addActionListener(e -> {
            String email = testEmailField.getText().trim();
            if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Email দিন!"); return; }
            templateStatus.setForeground(new Color(30, 100, 200));
            templateStatus.setText("Sending...");
            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                protected Boolean doInBackground() {
                    return EmailService.sendDepositNotification(
                            email, "Test Customer", "ACC123456789", "5000.00", "25000.00");
                }
                protected void done() {
                    try {
                        boolean ok = get();
                        templateStatus.setForeground(ok ? new Color(0, 130, 80) : new Color(200, 50, 50));
                        templateStatus.setText(ok ? "✔ Deposit email পাঠানো হয়েছে!" : "✘ Failed!");
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            };
            w.execute();
        });

        withdrawBtn.addActionListener(e -> {
            String email = testEmailField.getText().trim();
            if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Email দিন!"); return; }
            templateStatus.setText("Sending...");
            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                protected Boolean doInBackground() {
                    return EmailService.sendWithdrawalNotification(
                            email, "Test Customer", "ACC123456789", "2000.00", "23000.00");
                }
                protected void done() {
                    try {
                        boolean ok = get();
                        templateStatus.setForeground(ok ? new Color(0, 130, 80) : new Color(200, 50, 50));
                        templateStatus.setText(ok ? "✔ Withdrawal email পাঠানো হয়েছে!" : "✘ Failed!");
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            };
            w.execute();
        });

        loanBtn.addActionListener(e -> {
            String email = testEmailField.getText().trim();
            if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Email দিন!"); return; }
            templateStatus.setText("Sending...");
            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                protected Boolean doInBackground() {
                    return EmailService.sendLoanApprovalNotification(
                            email, "Test Customer", "100000.00", "9166.00");
                }
                protected void done() {
                    try {
                        boolean ok = get();
                        templateStatus.setForeground(ok ? new Color(0, 130, 80) : new Color(200, 50, 50));
                        templateStatus.setText(ok ? "✔ Loan email পাঠানো হয়েছে!" : "✘ Failed!");
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            };
            w.execute();
        });

        return panel;
    }

    private void sendCustomEmail() {
        String toEmail  = toEmailField.getText().trim();
        String subject  = subjectField.getText().trim();
        String body     = bodyArea.getText().trim();

        if (toEmail.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            JOptionPane.showMessageDialog(this, "সব field পূরণ করো!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setForeground(new Color(30, 100, 200));
        statusLabel.setText("Sending...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            protected Boolean doInBackground() {
                return EmailService.sendEmail(toEmail, subject,
                        "<pre style='font-family:Arial;'>" + body + "</pre>");
            }
            protected void done() {
                try {
                    boolean ok = get();
                    statusLabel.setForeground(ok ? new Color(0, 130, 80) : new Color(200, 50, 50));
                    statusLabel.setText(ok ?
                            "✔ Email সফলভাবে পাঠানো হয়েছে!" :
                            "✘ Failed! Gmail App Password check করো।");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }
}