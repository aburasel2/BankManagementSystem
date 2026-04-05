package view;

import controller.CustomerController;
import controller.LoanController;
import model.Customer;
import model.Loan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class LoanPanel extends JPanel {

    private JComboBox<String> customerCombo;
    private JTextField accountField, amountField, interestField,
            durationField, paymentLoanIdField, paymentAmountField;
    private JTable loanTable;
    private DefaultTableModel tableModel;
    private LoanController loanController;
    private CustomerController customerController;
    private List<Customer> customerList;

    public LoanPanel() {
        loanController    = new LoanController();
        customerController = new CustomerController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        loadLoans();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Loan Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 90));
        add(title, BorderLayout.NORTH);

        // Tabbed Pane — Apply, Manage, Payment আলাদা tab এ
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));

        tabs.addTab("Apply Loan",    buildApplyPanel());
        tabs.addTab("Manage Loans",  buildManagePanel());
        tabs.addTab("Make Payment",  buildPaymentPanel());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Loan Apply ──
    private JPanel buildApplyPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 4, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Customer
        panel.add(new JLabel("Customer:"));
        customerCombo = new JComboBox<>();
        loadCustomerCombo();
        panel.add(customerCombo);
        panel.add(new JLabel("Account Number:"));
        accountField = new JTextField();
        accountField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(accountField);

        // Loan Amount
        panel.add(new JLabel("Loan Amount (TK):"));
        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(amountField);

        // Interest Rate
        panel.add(new JLabel("Interest Rate (%):"));
        interestField = new JTextField("10");
        interestField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(interestField);

        // Duration
        panel.add(new JLabel("Duration (Months):"));
        durationField = new JTextField("12");
        durationField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(durationField);

        // Monthly payment preview
        panel.add(new JLabel(""));
        JButton calcBtn = createButton("Calculate Monthly", new Color(100, 100, 180));
        panel.add(calcBtn);
        JLabel monthlyLabel = new JLabel("Monthly: --");
        monthlyLabel.setFont(new Font("Arial", Font.BOLD, 13));
        monthlyLabel.setForeground(new Color(30, 100, 200));
        panel.add(monthlyLabel);
        panel.add(new JLabel(""));

        // Buttons
        JButton applyBtn = createButton("Apply Loan", new Color(0, 150, 100));
        JButton clearBtn = createButton("Clear",      new Color(120, 120, 120));
        panel.add(applyBtn);
        panel.add(clearBtn);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        // Info
        JLabel info = new JLabel("  ℹ️  Loan apply করলে Pending status এ থাকবে। Admin Approve করলে টাকা account এ যাবে।");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        info.setForeground(new Color(100, 100, 100));
        info.setOpaque(true);
        info.setBackground(new Color(240, 248, 255));
        panel.add(info);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        // Button Actions
        calcBtn.addActionListener(e -> {
            try {
                double amount   = Double.parseDouble(amountField.getText().trim());
                double rate     = Double.parseDouble(interestField.getText().trim());
                int    duration = Integer.parseInt(durationField.getText().trim());
                double r = (rate / 100) / 12;
                double monthly;
                if (r == 0) {
                    monthly = amount / duration;
                } else {
                    monthly = amount * (r * Math.pow(1+r, duration)) / (Math.pow(1+r, duration) - 1);
                }
                monthlyLabel.setText(String.format("Monthly: TK %.2f", monthly));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "সঠিক সংখ্যা দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        applyBtn.addActionListener(e -> applyLoan());
        clearBtn.addActionListener(e -> {
            customerCombo.setSelectedIndex(0);
            accountField.setText("");
            amountField.setText("");
            interestField.setText("10");
            durationField.setText("12");
            monthlyLabel.setText("Monthly: --");
        });

        return panel;
    }

    // ── Tab 2: Manage Loans ──
    private JPanel buildManagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // Table
        String[] columns = {"ID", "Customer", "Account", "Amount (TK)",
                "Rate%", "Months", "Monthly (TK)", "Paid (TK)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        loanTable = new JTable(tableModel);
        loanTable.setRowHeight(28);
        loanTable.setFont(new Font("Arial", Font.PLAIN, 13));
        loanTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        loanTable.setSelectionBackground(new Color(173, 216, 230));

        // Status column রঙ
        loanTable.getColumnModel().getColumn(8).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(value != null ? value.toString() : "");
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    switch (value != null ? value.toString() : "") {
                        case "Approved": lbl.setForeground(new Color(0, 130, 80)); break;
                        case "Pending":  lbl.setForeground(new Color(180, 100, 0)); break;
                        case "Rejected": lbl.setForeground(new Color(200, 50, 50)); break;
                        case "Closed":   lbl.setForeground(new Color(100, 100, 100)); break;
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane scroll = new JScrollPane(loanTable);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton approveBtn = createButton("Approve",  new Color(0, 150, 100));
        JButton rejectBtn  = createButton("Reject",   new Color(200, 50, 50));
        JButton refreshBtn = createButton("Refresh",  new Color(120, 120, 120));

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);

        // Button Actions
        approveBtn.addActionListener(e -> {
            int row = loanTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "আগে একটা loan select করো!");
                return;
            }
            int loanId = (int) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 8);
            if (!"Pending".equals(status)) {
                JOptionPane.showMessageDialog(this, "শুধু Pending loan Approve করা যাবে!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Loan ID " + loanId + " Approve করবে? Account এ টাকা যাবে।",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (loanController.approveLoan(loanId)) {
                    JOptionPane.showMessageDialog(this, "Loan Approved! Account এ টাকা গেছে।");
                    loadLoans();
                } else {
                    JOptionPane.showMessageDialog(this, "Approve failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        rejectBtn.addActionListener(e -> {
            int row = loanTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "আগে একটা loan select করো!");
                return;
            }
            int loanId = (int) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 8);
            if (!"Pending".equals(status)) {
                JOptionPane.showMessageDialog(this, "শুধু Pending loan Reject করা যাবে!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Loan ID " + loanId + " Reject করবে?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (loanController.rejectLoan(loanId)) {
                    JOptionPane.showMessageDialog(this, "Loan Rejected!");
                    loadLoans();
                }
            }
        });

        refreshBtn.addActionListener(e -> loadLoans());

        return panel;
    }

    // ── Tab 3: Make Payment ──
    private JPanel buildPaymentPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 4, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Loan ID:"));
        paymentLoanIdField = new JTextField();
        paymentLoanIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(paymentLoanIdField);
        panel.add(new JLabel("Payment Amount (TK):"));
        paymentAmountField = new JTextField();
        paymentAmountField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(paymentAmountField);

        JButton payBtn   = createButton("Make Payment", new Color(0, 150, 100));
        JButton clearBtn = createButton("Clear",        new Color(120, 120, 120));
        panel.add(payBtn);
        panel.add(clearBtn);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        JLabel info = new JLabel("  ℹ️  Loan ID টা Manage Loans tab থেকে দেখো।");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        info.setForeground(new Color(100, 100, 100));
        info.setOpaque(true);
        info.setBackground(new Color(240, 248, 255));
        panel.add(info);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        payBtn.addActionListener(e -> makePayment());
        clearBtn.addActionListener(e -> {
            paymentLoanIdField.setText("");
            paymentAmountField.setText("");
        });

        return panel;
    }

    // ── Helper Methods ──
    private void loadCustomerCombo() {
        customerList = customerController.getAllCustomers();
        customerCombo.removeAllItems();
        customerCombo.addItem("-- Select Customer --");
        for (Customer c : customerList) {
            customerCombo.addItem(c.getId() + " - " + c.getFullName());
        }
    }

    private void loadLoans() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        List<Loan> list = loanController.getAllLoans();
        for (Loan l : list) {
            tableModel.addRow(new Object[]{
                    l.getId(),
                    l.getCustomerName(),
                    l.getAccountNumber(),
                    l.getLoanAmount(),
                    l.getInterestRate(),
                    l.getDurationMonths(),
                    l.getMonthlyPayment(),
                    l.getAmountPaid(),
                    l.getStatus()
            });
        }
    }

    private void applyLoan() {
        if (customerCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Customer select করো!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String selected  = (String) customerCombo.getSelectedItem();
            int customerId   = Integer.parseInt(selected.split(" - ")[0]);
            String accNum    = accountField.getText().trim();
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            double interest  = Double.parseDouble(interestField.getText().trim());
            int duration     = Integer.parseInt(durationField.getText().trim());

            if (accNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (loanController.applyLoan(customerId, accNum, amount, interest, duration)) {
                JOptionPane.showMessageDialog(this,
                        "Loan application সফলভাবে submit হয়েছে!\nAdmin Approve করলে account এ টাকা যাবে।");
                loadLoans();
            } else {
                JOptionPane.showMessageDialog(this, "Loan apply failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক সংখ্যা দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makePayment() {
        try {
            int loanId        = Integer.parseInt(paymentLoanIdField.getText().trim());
            BigDecimal amount = new BigDecimal(paymentAmountField.getText().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String result = loanController.makeLoanPayment(loanId, amount);
            if (result.startsWith("SUCCESS")) {
                String status = result.split(":")[1];
                String msg = "Payment সফল!";
                if ("Closed".equals(status)) msg += "\n🎉 Loan সম্পূর্ণ পরিশোধ হয়ে গেছে!";
                JOptionPane.showMessageDialog(this, msg);
                paymentLoanIdField.setText("");
                paymentAmountField.setText("");
                loadLoans();
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক Loan ID এবং Amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 36));
        return btn;
    }
}