package view;

import controller.StatementController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;

public class StatementPanel extends JPanel {

    private JTextField accountField, fromDateField, toDateField;
    private JLabel statusLabel;
    private StatementController statementController;

    public StatementPanel() {
        statementController = new StatementController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Account Statement");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(15, 40, 80));
        add(title, BorderLayout.NORTH);

        // Center — form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Icon
        JLabel icon = new JLabel("📄");
        icon.setFont(new Font("Arial", Font.PLAIN, 60));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(icon, gbc);

        // Subtitle
        JLabel sub = new JLabel("Account Statement PDF তৈরি করো");
        sub.setFont(new Font("Arial", Font.BOLD, 16));
        sub.setForeground(new Color(15, 40, 80));
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        centerPanel.add(sub, gbc);

        // Form box
        JPanel formBox = new JPanel(new GridLayout(4, 2, 12, 12));
        formBox.setBackground(new Color(245, 248, 255));
        formBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
                new EmptyBorder(20, 30, 20, 30)));

        // Account number
        JLabel accLabel = new JLabel("Account Number:");
        accLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formBox.add(accLabel);
        accountField = new JTextField();
        accountField.setFont(new Font("Arial", Font.PLAIN, 14));
        accountField.setPreferredSize(new Dimension(250, 36));
        formBox.add(accountField);

        // From date
        JLabel fromLabel = new JLabel("From Date (YYYY-MM-DD):");
        fromLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formBox.add(fromLabel);
        fromDateField = new JTextField(
                new java.text.SimpleDateFormat("yyyy-MM-01").format(new java.util.Date()));
        fromDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        formBox.add(fromDateField);

        // To date
        JLabel toLabel = new JLabel("To Date (YYYY-MM-DD):");
        toLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formBox.add(toLabel);
        toDateField = new JTextField(
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        toDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        formBox.add(toDateField);

        // Buttons
        JButton generateBtn = createButton("Generate PDF", new Color(15, 40, 80));
        JButton clearBtn    = createButton("Clear",        new Color(120, 120, 120));
        formBox.add(generateBtn);
        formBox.add(clearBtn);

        gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(formBox, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        centerPanel.add(statusLabel, gbc);

        // Info
        JLabel info = new JLabel(
                "<html><center><i>PDF টা Desktop এ save হবে এবং automatically খুলবে।</i></center></html>");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        info.setForeground(new Color(130, 140, 160));
        info.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        centerPanel.add(info, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Actions
        generateBtn.addActionListener(e -> generatePDF());
        clearBtn.addActionListener(e -> {
            accountField.setText("");
            statusLabel.setText(" ");
        });
    }

    private void generatePDF() {
        String accNum   = accountField.getText().trim();
        String fromDate = fromDateField.getText().trim();
        String toDate   = toDateField.getText().trim();

        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Date range দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save path — Desktop খুঁজে না পেলে Documents এ save হবে
        String userHome    = System.getProperty("user.home");
        String desktopPath = userHome + "\\Desktop";
        File   desktopDir  = new File(desktopPath);

        if (!desktopDir.exists()) {
            desktopPath = userHome + "\\Documents";
            new File(desktopPath).mkdirs();
        }

        String fileName = "Statement_" + accNum + "_" +
                new java.text.SimpleDateFormat("yyyyMMdd_HHmm").format(new java.util.Date()) + ".pdf";
        String savePath = desktopPath + "\\" + fileName;

        statusLabel.setForeground(new Color(30, 100, 200));
        statusLabel.setText("PDF তৈরি হচ্ছে...");

        final String finalSavePath = savePath;
        final String finalFileName = fileName;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                return statementController.generateStatement(
                        accNum, fromDate, toDate, finalSavePath);
            }
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("SUCCESS")) {
                        statusLabel.setForeground(new Color(0, 130, 80));
                        statusLabel.setText("✔ PDF সফলভাবে তৈরি হয়েছে: " + finalFileName);
                        JOptionPane.showMessageDialog(StatementPanel.this,
                                "Statement PDF তৈরি হয়েছে!\n\nSaved: " + finalSavePath,
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        // PDF automatically খোলো
                        try {
                            Desktop.getDesktop().open(new File(finalSavePath));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(StatementPanel.this,
                                    "PDF এখানে save হয়েছে:\n" + finalSavePath);
                        }
                    } else {
                        statusLabel.setForeground(new Color(200, 50, 50));
                        statusLabel.setText("Error: " + result);
                        JOptionPane.showMessageDialog(StatementPanel.this,
                                result, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }
}