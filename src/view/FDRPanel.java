package view;

import controller.CustomerController;
import controller.FDRController;
import model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FDRPanel extends JPanel {

    private JComboBox<String> customerCombo;
    private JTextField accountField, amountField, rateField, durationField;
    private JLabel maturityLabel;
    private JTable fdrTable;
    private DefaultTableModel tableModel;
    private FDRController fdrController;
    private CustomerController customerController;
    private List<Customer> customerList;

    public FDRPanel() {
        fdrController      = new FDRController();
        customerController = new CustomerController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
        fdrController.checkAndUpdateMaturedFDRs();
        loadFDRs();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Fixed Deposit (FDR) Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(15, 40, 80));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("Create FDR",  buildCreatePanel());
        tabs.addTab("FDR List",    buildListPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Create FDR ──
    private JPanel buildCreatePanel() {
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

        // Amount
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Principal Amount (TK):"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(amountField, gbc);

        // Interest Rate
        gbc.gridx = 2;
        panel.add(new JLabel("Interest Rate (%):"), gbc);
        gbc.gridx = 3;
        rateField = new JTextField("10");
        rateField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(rateField, gbc);

        // Duration
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Duration (Months):"), gbc);
        gbc.gridx = 1;
        durationField = new JTextField("12");
        durationField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(durationField, gbc);

        // Calculate button
        gbc.gridx = 2;
        JButton calcBtn = createButton("Calculate", new Color(30, 100, 200));
        panel.add(calcBtn, gbc);

        // Maturity label
        gbc.gridx = 3;
        maturityLabel = new JLabel("Maturity: --");
        maturityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        maturityLabel.setForeground(new Color(0, 130, 80));
        panel.add(maturityLabel, gbc);

        // Info box
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JPanel infoBox = new JPanel(new GridLayout(3, 1, 0, 2));
        infoBox.setBackground(new Color(240, 248, 255));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 240)),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel i1 = new JLabel("ℹ️  FDR তৈরি করলে Account থেকে টাকা কাটা যাবে।");
        JLabel i2 = new JLabel("ℹ️  Maturity date এর আগে encash করলে শুধু principal ফেরত পাবে।");
        JLabel i3 = new JLabel("ℹ️  Maturity date এর পরে encash করলে সুদসহ সম্পূর্ণ টাকা পাবে।");
        for (JLabel l : new JLabel[]{i1, i2, i3}) {
            l.setFont(new Font("Arial", Font.PLAIN, 12));
            l.setForeground(new Color(60, 80, 120));
            infoBox.add(l);
        }
        panel.add(infoBox, gbc);

        // Buttons
        gbc.gridy = 4; gbc.gridwidth = 2;
        JButton createBtn = createButton("Create FDR", new Color(0, 150, 100));
        createBtn.setPreferredSize(new Dimension(200, 40));
        panel.add(createBtn, gbc);

        gbc.gridx = 2;
        JButton clearBtn = createButton("Clear", new Color(120, 120, 120));
        panel.add(clearBtn, gbc);

        // Actions
        calcBtn.addActionListener(e -> calculateMaturity());
        createBtn.addActionListener(e -> createFDR());
        clearBtn.addActionListener(e -> clearFields());

        return panel;
    }

    // ── Tab 2: FDR List ──
    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        // Table
        String[] columns = {"ID", "FDR Number", "Customer", "Account",
                "Principal (TK)", "Rate %", "Months",
                "Maturity (TK)", "Start Date", "Maturity Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        fdrTable = new JTable(tableModel);
        fdrTable.setRowHeight(28);
        fdrTable.setFont(new Font("Arial", Font.PLAIN, 13));
        fdrTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        fdrTable.setSelectionBackground(new Color(173, 216, 230));

        // Status column রঙ
        fdrTable.getColumnModel().getColumn(10).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(" " + (value != null ? value : ""));
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    switch (value != null ? value.toString() : "") {
                        case "Active":  lbl.setForeground(new Color(0, 130, 80));  break;
                        case "Matured": lbl.setForeground(new Color(180, 100, 0)); break;
                        case "Closed":  lbl.setForeground(new Color(150, 150, 150)); break;
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane scroll = new JScrollPane(fdrTable);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton encashBtn  = createButton("Encash FDR",  new Color(0, 150, 100));
        JButton refreshBtn = createButton("Refresh",     new Color(120, 120, 120));
        btnPanel.add(encashBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);

        // Actions
        encashBtn.addActionListener(e -> encashFDR());
        refreshBtn.addActionListener(e -> {
            fdrController.checkAndUpdateMaturedFDRs();
            loadFDRs();
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

    private void loadFDRs() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        List<Map<String, Object>> list = fdrController.getAllFDRs();
        for (Map<String, Object> row : list) {
            tableModel.addRow(new Object[]{
                    row.get("id"),
                    row.get("fdr_number"),
                    row.get("customer_name"),
                    row.get("account_number"),
                    row.get("principal_amount"),
                    row.get("interest_rate"),
                    row.get("duration_months"),
                    row.get("maturity_amount"),
                    row.get("start_date"),
                    row.get("maturity_date"),
                    row.get("status")
            });
        }
    }

    private void calculateMaturity() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            double rate       = Double.parseDouble(rateField.getText().trim());
            int months        = Integer.parseInt(durationField.getText().trim());
            BigDecimal maturity = fdrController.calculateMaturity(amount, rate, months);
            BigDecimal interest = maturity.subtract(amount);
            maturityLabel.setText(String.format(
                    "Maturity: TK %.2f  (+TK %.2f interest)", maturity, interest));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক সংখ্যা দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createFDR() {
        if (customerCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Customer select করো!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String accNum = accountField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String selected   = (String) customerCombo.getSelectedItem();
            int customerId    = Integer.parseInt(selected.split(" - ")[0]);
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            double rate       = Double.parseDouble(rateField.getText().trim());
            int months        = Integer.parseInt(durationField.getText().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal maturity = fdrController.calculateMaturity(amount, rate, months);

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("FDR তৈরি করবে?\n\nPrincipal: TK %.2f\nRate: %.2f%%\nDuration: %d months\nMaturity: TK %.2f",
                            amount, rate, months, maturity),
                    "Confirm FDR", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String result = fdrController.createFDR(customerId, accNum, amount, rate, months);
                if (result.startsWith("SUCCESS")) {
                    String fdrNum = result.split(":")[1];
                    JOptionPane.showMessageDialog(this,
                            "FDR সফলভাবে তৈরি হয়েছে!\nFDR Number: " + fdrNum);
                    clearFields();
                    loadFDRs();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক সংখ্যা দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void encashFDR() {
        int row = fdrTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "আগে একটা FDR select করো!");
            return;
        }
        int    fdrId  = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 10);
        String maturityDate = tableModel.getValueAt(row, 9).toString();

        if ("Closed".equals(status)) {
            JOptionPane.showMessageDialog(this, "এই FDR already closed!");
            return;
        }

        String msg = "Matured".equals(status) ?
                "FDR matured হয়েছে! সুদসহ টাকা account এ যাবে। Encash করবে?" :
                "FDR এখনো mature হয়নি (Maturity: " + maturityDate + ")!\nEarly encash করলে শুধু principal ফেরত পাবে। Continue করবে?";

        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Encash", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String result = fdrController.encashFDR(fdrId);
            if (result.startsWith("SUCCESS")) {
                String amount = result.split(":")[1];
                JOptionPane.showMessageDialog(this,
                        "FDR Encash সফল!\n" + amount + " account এ যোগ হয়েছে।");
                loadFDRs();
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        customerCombo.setSelectedIndex(0);
        accountField.setText("");
        amountField.setText("");
        rateField.setText("10");
        durationField.setText("12");
        maturityLabel.setText("Maturity: --");
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