package view;

import controller.TransactionController;
import model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransactionHistoryPanel extends JPanel {

    private JTextField searchAccountField;
    private JComboBox<String> filterCombo;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private TransactionController transactionController;
    private JLabel totalDepositLabel, totalWithdrawLabel, totalTransferLabel;

    public TransactionHistoryPanel() {
        transactionController = new TransactionController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        loadAllTransactions();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 90));
        add(title, BorderLayout.NORTH);

        // Top Panel — Search + Filter
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        topPanel.setBackground(Color.WHITE);

        // Search Row
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        searchPanel.add(new JLabel("Account Number:"));
        searchAccountField = new JTextField(20);
        searchAccountField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(searchAccountField);

        searchPanel.add(new JLabel("Type:"));
        filterCombo = new JComboBox<>(new String[]{"All", "Deposit", "Withdrawal", "Transfer"});
        filterCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        filterCombo.setPreferredSize(new Dimension(120, 30));
        searchPanel.add(filterCombo);

        JButton searchBtn  = createButton("Search",   new Color(30, 100, 200));
        JButton allBtn     = createButton("Show All", new Color(100, 100, 180));
        JButton clearBtn   = createButton("Clear",    new Color(120, 120, 120));
        searchPanel.add(searchBtn);
        searchPanel.add(allBtn);
        searchPanel.add(clearBtn);

        // Summary Row
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        summaryPanel.setBackground(new Color(245, 248, 255));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        totalDepositLabel  = new JLabel("Total Deposit: TK 0.00");
        totalWithdrawLabel = new JLabel("Total Withdraw: TK 0.00");
        totalTransferLabel = new JLabel("Total Transfer: TK 0.00");

        totalDepositLabel.setFont(new Font("Arial", Font.BOLD, 13));
        totalWithdrawLabel.setFont(new Font("Arial", Font.BOLD, 13));
        totalTransferLabel.setFont(new Font("Arial", Font.BOLD, 13));

        totalDepositLabel.setForeground(new Color(0, 130, 80));
        totalWithdrawLabel.setForeground(new Color(200, 50, 50));
        totalTransferLabel.setForeground(new Color(30, 100, 200));

        summaryPanel.add(totalDepositLabel);
        summaryPanel.add(new JSeparator(JSeparator.VERTICAL));
        summaryPanel.add(totalWithdrawLabel);
        summaryPanel.add(new JSeparator(JSeparator.VERTICAL));
        summaryPanel.add(totalTransferLabel);

        topPanel.add(searchPanel);
        topPanel.add(summaryPanel);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Account No", "Type", "Amount (TK)", "Balance After (TK)", "Description", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.setSelectionBackground(new Color(173, 216, 230));

        // Type column রঙ
        historyTable.getColumnModel().getColumn(2).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(value != null ? " " + value.toString() : "");
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    switch (value != null ? value.toString() : "") {
                        case "Deposit":    lbl.setForeground(new Color(0, 130, 80));  break;
                        case "Withdrawal": lbl.setForeground(new Color(200, 50, 50)); break;
                        case "Transfer":   lbl.setForeground(new Color(30, 100, 200)); break;
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        // Amount column রঙ
        historyTable.getColumnModel().getColumn(3).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(value != null ? "TK " + value.toString() : "");
                    lbl.setFont(new Font("Arial", Font.PLAIN, 13));
                    lbl.setOpaque(true);
                    String type = (String) tableModel.getValueAt(row, 2);
                    if ("Deposit".equals(type)) {
                        lbl.setForeground(new Color(0, 130, 80));
                    } else if ("Withdrawal".equals(type)) {
                        lbl.setForeground(new Color(200, 50, 50));
                    } else {
                        lbl.setForeground(new Color(30, 100, 200));
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("All Transactions"));
        add(tableScroll, BorderLayout.CENTER);

        // Row count label
        JLabel countLabel = new JLabel("  Total: 0 transactions");
        countLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        countLabel.setForeground(new Color(100, 100, 100));
        add(countLabel, BorderLayout.SOUTH);

        // Button Actions
        searchBtn.addActionListener(e -> searchTransactions(countLabel));
        allBtn.addActionListener(e -> {
            searchAccountField.setText("");
            filterCombo.setSelectedIndex(0);
            loadAllTransactions();
            countLabel.setText("  Total: " + tableModel.getRowCount() + " transactions");
        });
        clearBtn.addActionListener(e -> {
            searchAccountField.setText("");
            filterCombo.setSelectedIndex(0);
            tableModel.setRowCount(0);
            totalDepositLabel.setText("Total Deposit: TK 0.00");
            totalWithdrawLabel.setText("Total Withdraw: TK 0.00");
            totalTransferLabel.setText("Total Transfer: TK 0.00");
            countLabel.setText("  Total: 0 transactions");
        });
    }

    private void loadAllTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> list = transactionController.getAllTransactions();
        double deposit = 0, withdraw = 0, transfer = 0;

        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getAccountNumber(),
                    t.getType(),
                    t.getAmount(),
                    t.getBalanceAfter(),
                    t.getDescription(),
                    t.getTransactionDate()
            });
            switch (t.getType()) {
                case "Deposit":    deposit  += t.getAmount().doubleValue(); break;
                case "Withdrawal": withdraw += t.getAmount().doubleValue(); break;
                case "Transfer":   transfer += t.getAmount().doubleValue(); break;
            }
        }
        totalDepositLabel.setText(String.format("Total Deposit: TK %.2f", deposit));
        totalWithdrawLabel.setText(String.format("Total Withdraw: TK %.2f", withdraw));
        totalTransferLabel.setText(String.format("Total Transfer: TK %.2f", transfer));
    }

    private void searchTransactions(JLabel countLabel) {
        String accNum = searchAccountField.getText().trim();
        String type   = (String) filterCombo.getSelectedItem();

        tableModel.setRowCount(0);
        List<Transaction> list = transactionController.getAllTransactions();
        double deposit = 0, withdraw = 0, transfer = 0;

        for (Transaction t : list) {
            boolean matchAcc  = accNum.isEmpty() || t.getAccountNumber().contains(accNum);
            boolean matchType = "All".equals(type) || t.getType().equals(type);

            if (matchAcc && matchType) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getAccountNumber(),
                        t.getType(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        t.getDescription(),
                        t.getTransactionDate()
                });
                switch (t.getType()) {
                    case "Deposit":    deposit  += t.getAmount().doubleValue(); break;
                    case "Withdrawal": withdraw += t.getAmount().doubleValue(); break;
                    case "Transfer":   transfer += t.getAmount().doubleValue(); break;
                }
            }
        }
        totalDepositLabel.setText(String.format("Total Deposit: TK %.2f", deposit));
        totalWithdrawLabel.setText(String.format("Total Withdraw: TK %.2f", withdraw));
        totalTransferLabel.setText(String.format("Total Transfer: TK %.2f", transfer));
        countLabel.setText("  Total: " + tableModel.getRowCount() + " transactions");
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 34));
        return btn;
    }
}