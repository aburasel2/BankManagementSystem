package view;

import controller.TransactionController;
import model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class TransactionPanel extends JPanel {

    private JTextField accountField, amountField, descriptionField;
    private JLabel balanceLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private TransactionController transactionController;

    public TransactionPanel() {
        transactionController = new TransactionController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Deposit & Withdraw");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 90));
        add(title, BorderLayout.NORTH);

        // Form Panel — GridLayout ব্যবহার করছি, সহজ এবং field ঠিকঠাক দেখায়
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Transaction"));

        // Row 1: Account Number + Check Balance + Balance
        formPanel.add(new JLabel("Account Number:"));
        accountField = new JTextField();
        accountField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(accountField);

        JButton checkBtn = createButton("Check Balance", new Color(30, 100, 200));
        formPanel.add(checkBtn);

        balanceLabel = new JLabel("Balance: --");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        balanceLabel.setForeground(new Color(0, 130, 80));
        formPanel.add(balanceLabel);

        // Row 2: Amount + Description
        formPanel.add(new JLabel("Amount (TK):"));
        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(amountField);

        formPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(descriptionField);

        // Row 3: Buttons
        JButton depositBtn  = createButton("Deposit",  new Color(0, 150, 100));
        JButton withdrawBtn = createButton("Withdraw", new Color(200, 50, 50));
        JButton historyBtn  = createButton("History",  new Color(100, 100, 180));
        JButton clearBtn    = createButton("Clear",    new Color(120, 120, 120));

        formPanel.add(depositBtn);
        formPanel.add(withdrawBtn);
        formPanel.add(historyBtn);
        formPanel.add(clearBtn);

        // History Table
        String[] columns = {"ID", "Type", "Amount", "Balance After", "Description", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.setSelectionBackground(new Color(173, 216, 230));

        // Type column রঙ
        historyTable.getColumnModel().getColumn(1).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(value != null ? value.toString() : "");
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    if ("Deposit".equals(value)) {
                        lbl.setForeground(new Color(0, 130, 80));
                    } else if ("Withdrawal".equals(value)) {
                        lbl.setForeground(new Color(200, 50, 50));
                    } else {
                        lbl.setForeground(new Color(30, 100, 200));
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Transaction History"));

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Button Actions
        checkBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> doDeposit());
        withdrawBtn.addActionListener(e -> doWithdraw());
        historyBtn.addActionListener(e -> loadHistory());
        clearBtn.addActionListener(e -> clearFields());
    }

    private void checkBalance() {
        String accNum = accountField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BigDecimal balance = transactionController.getBalance(accNum);
        if (balance != null) {
            balanceLabel.setText("Balance: TK " + balance);
        } else {
            balanceLabel.setText("Balance: --");
            JOptionPane.showMessageDialog(this, "Account পাওয়া যায়নি!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDeposit() {
        String accNum = accountField.getText().trim();
        String amountText = amountField.getText().trim();
        String desc = descriptionField.getText().trim();

        if (accNum.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number এবং Amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (transactionController.deposit(accNum, amount, desc.isEmpty() ? "Cash deposit" : desc)) {
                JOptionPane.showMessageDialog(this, amount + " TK সফলভাবে Deposit হয়েছে!");
                checkBalance();
                loadHistory();
                amountField.setText("");
                descriptionField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Deposit failed! Account active আছে কিনা দেখো।", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doWithdraw() {
        String accNum = accountField.getText().trim();
        String amountText = amountField.getText().trim();

        if (accNum.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number এবং Amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String result = transactionController.withdraw(accNum, amount);
            if ("SUCCESS".equals(result)) {
                JOptionPane.showMessageDialog(this, amount + " TK সফলভাবে Withdraw হয়েছে!");
                checkBalance();
                loadHistory();
                amountField.setText("");
                descriptionField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHistory() {
        String accNum = accountField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        List<Transaction> list = transactionController.getHistory(accNum);
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "কোনো transaction পাওয়া যায়নি!");
            return;
        }
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getType(),
                    t.getAmount(),
                    t.getBalanceAfter(),
                    t.getDescription(),
                    t.getTransactionDate()
            });
        }
    }

    private void clearFields() {
        accountField.setText("");
        amountField.setText("");
        descriptionField.setText("");
        balanceLabel.setText("Balance: --");
        tableModel.setRowCount(0);
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