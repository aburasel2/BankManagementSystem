package view;

import controller.ATMController;
import controller.CustomerController;
import model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ATMPanel extends JPanel {

    private JComboBox<String> customerCombo, cardTypeCombo;
    private JTextField accountField, pinField, dailyLimitField;
    private JTable cardTable;
    private DefaultTableModel tableModel;
    private ATMController atmController;
    private CustomerController customerController;
    private List<Customer> customerList;

    public ATMPanel() {
        atmController      = new ATMController();
        customerController = new CustomerController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
        atmController.checkAndExpireCards();
        loadCards();
    }

    private void initUI() {
        JLabel title = new JLabel("ATM Card Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(15, 40, 80));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("Issue Card", buildIssuePanel());
        tabs.addTab("Manage Cards", buildManagePanel());
        tabs.addTab("PIN Change", buildPinPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Issue Card ──
    private JPanel buildIssuePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Customer
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1;
        customerCombo = new JComboBox<>();
        loadCustomerCombo();
        customerCombo.setPreferredSize(new Dimension(250, 32));
        panel.add(customerCombo, gbc);

        // Account
        gbc.gridx = 2;
        panel.add(new JLabel("Account Number:"), gbc);
        gbc.gridx = 3;
        accountField = new JTextField();
        accountField.setFont(new Font("Arial", Font.PLAIN, 14));
        accountField.setPreferredSize(new Dimension(200, 32));
        panel.add(accountField, gbc);

        // Card Type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Card Type:"), gbc);
        gbc.gridx = 1;
        cardTypeCombo = new JComboBox<>(new String[]{"Debit", "Credit"});
        cardTypeCombo.setPreferredSize(new Dimension(250, 32));
        panel.add(cardTypeCombo, gbc);

        // PIN
        gbc.gridx = 2;
        panel.add(new JLabel("PIN (4 digits):"), gbc);
        gbc.gridx = 3;
        pinField = new JTextField();
        pinField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(pinField, gbc);

        // Daily Limit
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Daily Limit (TK):"), gbc);
        gbc.gridx = 1;
        dailyLimitField = new JTextField("20000");
        dailyLimitField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(dailyLimitField, gbc);

        // Card preview
        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel cardPreview = buildCardPreview();
        panel.add(cardPreview, gbc);

        // Info
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JLabel info = new JLabel(
                "  ℹ️  Card issue করলে 3 বছরের জন্য valid থাকবে। PIN 4 digit হতে হবে।");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        info.setForeground(new Color(80, 100, 140));
        info.setOpaque(true);
        info.setBackground(new Color(240, 248, 255));
        panel.add(info, gbc);

        // Buttons
        gbc.gridy = 4; gbc.gridwidth = 2;
        JButton issueBtn = createButton("Issue Card", new Color(0, 150, 100));
        issueBtn.setPreferredSize(new Dimension(200, 40));
        panel.add(issueBtn, gbc);
        gbc.gridx = 2;
        JButton clearBtn = createButton("Clear", new Color(120, 120, 120));
        panel.add(clearBtn, gbc);

        issueBtn.addActionListener(e -> issueCard());
        clearBtn.addActionListener(e -> clearFields());

        return panel;
    }

    // ATM Card visual preview
    private JPanel buildCardPreview() {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(new Color(15, 40, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                // Chip
                g2.setColor(new Color(220, 180, 50));
                g2.fillRoundRect(15, 30, 35, 28, 5, 5);
                // Card number
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Courier New", Font.BOLD, 14));
                g2.drawString("4XXX  XXXX  XXXX  XXXX", 12, 85);
                // Labels
                g2.setFont(new Font("Arial", Font.PLAIN, 9));
                g2.setColor(new Color(180, 200, 230));
                g2.drawString("CARD HOLDER", 12, 105);
                g2.drawString("VALID THRU", 140, 105);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.drawString("BANGLADESH BANK", 12, 118);
                g2.drawString("XX/XX", 140, 118);
                // Visa logo
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.setColor(new Color(255, 220, 50));
                g2.drawString("VISA", 170, 40);
            }
        };
        card.setPreferredSize(new Dimension(220, 135));
        card.setOpaque(false);
        return card;
    }

    // ── Tab 2: Manage Cards ──
    private JPanel buildManagePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"ID", "Card Number", "Customer", "Account",
                "Type", "Daily Limit", "Expiry", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cardTable = new JTable(tableModel);
        cardTable.setRowHeight(28);
        cardTable.setFont(new Font("Arial", Font.PLAIN, 13));
        cardTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        cardTable.setSelectionBackground(new Color(173, 216, 230));

        // Status column রঙ
        cardTable.getColumnModel().getColumn(7).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(" " + (value != null ? value : ""));
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    switch (value != null ? value.toString() : "") {
                        case "Active":  lbl.setForeground(new Color(0, 130, 80));   break;
                        case "Blocked": lbl.setForeground(new Color(200, 50, 50));  break;
                        case "Expired": lbl.setForeground(new Color(150, 150, 150)); break;
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane scroll = new JScrollPane(cardTable);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton blockBtn    = createButton("Block Card",   new Color(200, 50, 50));
        JButton unblockBtn  = createButton("Unblock Card", new Color(0, 150, 100));
        JButton refreshBtn  = createButton("Refresh",      new Color(120, 120, 120));

        // Limit update
        JLabel limitLabel = new JLabel("New Daily Limit:");
        limitLabel.setFont(new Font("Arial", Font.BOLD, 13));
        JTextField newLimitField = new JTextField("20000");
        newLimitField.setFont(new Font("Arial", Font.PLAIN, 13));
        newLimitField.setPreferredSize(new Dimension(100, 32));
        JButton updateLimitBtn = createButton("Update Limit", new Color(30, 100, 200));

        btnPanel.add(blockBtn);
        btnPanel.add(unblockBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(new JSeparator(JSeparator.VERTICAL));
        btnPanel.add(limitLabel);
        btnPanel.add(newLimitField);
        btnPanel.add(updateLimitBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);

        // Actions
        blockBtn.addActionListener(e -> {
            int row = cardTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Card select করো!"); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            if (atmController.updateCardStatus(id, "Blocked")) {
                JOptionPane.showMessageDialog(this, "Card blocked!");
                loadCards();
            }
        });

        unblockBtn.addActionListener(e -> {
            int row = cardTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Card select করো!"); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            if (atmController.updateCardStatus(id, "Active")) {
                JOptionPane.showMessageDialog(this, "Card unblocked!");
                loadCards();
            }
        });

        refreshBtn.addActionListener(e -> {
            atmController.checkAndExpireCards();
            loadCards();
        });

        updateLimitBtn.addActionListener(e -> {
            int row = cardTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Card select করো!"); return; }
            try {
                int id = (int) tableModel.getValueAt(row, 0);
                BigDecimal limit = new BigDecimal(newLimitField.getText().trim());
                if (atmController.updateDailyLimit(id, limit)) {
                    JOptionPane.showMessageDialog(this, "Daily limit update হয়েছে!");
                    loadCards();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "সঠিক amount দিন!");
            }
        });

        return panel;
    }

    // ── Tab 3: PIN Change ──
    private JPanel buildPinPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel icon = new JLabel("🔐");
        icon.setFont(new Font("Arial", Font.PLAIN, 50));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(icon, gbc);

        JLabel sub = new JLabel("ATM Card PIN Change");
        sub.setFont(new Font("Arial", Font.BOLD, 16));
        sub.setForeground(new Color(15, 40, 80));
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panel.add(sub, gbc);

        // Form
        JPanel formBox = new JPanel(new GridLayout(3, 2, 10, 10));
        formBox.setBackground(new Color(245, 248, 255));
        formBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 240)),
                new EmptyBorder(15, 20, 15, 20)));

        formBox.add(new JLabel("Card ID (Table থেকে দেখো):"));
        JTextField cardIdField = new JTextField();
        cardIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        formBox.add(cardIdField);

        formBox.add(new JLabel("Current PIN:"));
        JPasswordField oldPinField = new JPasswordField();
        oldPinField.setFont(new Font("Arial", Font.PLAIN, 14));
        formBox.add(oldPinField);

        formBox.add(new JLabel("New PIN (4 digits):"));
        JPasswordField newPinField = new JPasswordField();
        newPinField.setFont(new Font("Arial", Font.PLAIN, 14));
        formBox.add(newPinField);

        gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(formBox, gbc);

        JButton changeBtn = createButton("Change PIN", new Color(0, 150, 100));
        changeBtn.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 3;
        panel.add(changeBtn, gbc);

        changeBtn.addActionListener(e -> {
            try {
                int cardId     = Integer.parseInt(cardIdField.getText().trim());
                String oldPin  = new String(oldPinField.getPassword());
                String newPin  = new String(newPinField.getPassword());

                if (newPin.length() != 4 || !newPin.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this,
                            "New PIN অবশ্যই 4 digit number হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String result = atmController.changePin(cardId, oldPin, newPin);
                if ("SUCCESS".equals(result)) {
                    JOptionPane.showMessageDialog(this, "PIN সফলভাবে change হয়েছে!");
                    cardIdField.setText("");
                    oldPinField.setText("");
                    newPinField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "সঠিক Card ID দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private void loadCustomerCombo() {
        customerList = customerController.getAllCustomers();
        customerCombo.removeAllItems();
        customerCombo.addItem("-- Select Customer --");
        for (Customer c : customerList) {
            customerCombo.addItem(c.getId() + " - " + c.getFullName());
        }
    }

    private void loadCards() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        List<Map<String, Object>> list = atmController.getAllCards();
        for (Map<String, Object> row : list) {
            tableModel.addRow(new Object[]{
                    row.get("id"),
                    atmController.formatCardNumber(row.get("card_number").toString()),
                    row.get("customer_name"),
                    row.get("account_number"),
                    row.get("card_type"),
                    "TK " + row.get("daily_limit"),
                    row.get("expiry_date"),
                    row.get("status")
            });
        }
    }

    private void issueCard() {
        if (customerCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Customer select করো!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String accNum = accountField.getText().trim();
        String pin    = pinField.getText().trim();

        if (accNum.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Account number এবং PIN দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pin.length() != 4 || !pin.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                    "PIN অবশ্যই 4 digit number হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String selected    = (String) customerCombo.getSelectedItem();
            int customerId     = Integer.parseInt(selected.split(" - ")[0]);
            String cardType    = (String) cardTypeCombo.getSelectedItem();
            BigDecimal limit   = new BigDecimal(dailyLimitField.getText().trim());

            String result = atmController.issueCard(customerId, accNum, cardType, pin, limit);
            if (result.startsWith("SUCCESS")) {
                String cardNum = result.split(":")[1];
                JOptionPane.showMessageDialog(this,
                        "Card সফলভাবে issue হয়েছে!\n\n" +
                                "Card Number: " + atmController.formatCardNumber(cardNum) + "\n" +
                                "Type: " + cardType + "\n" +
                                "Daily Limit: TK " + limit + "\n" +
                                "Valid: 3 years");
                clearFields();
                loadCards();
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক Daily Limit দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        customerCombo.setSelectedIndex(0);
        accountField.setText("");
        pinField.setText("");
        dailyLimitField.setText("20000");
        cardTypeCombo.setSelectedIndex(0);
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