package view;

import controller.TransactionController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class FundTransferPanel extends JPanel {

    private JTextField fromAccountField, toAccountField, amountField, descriptionField;
    private JLabel fromBalanceLabel, toBalanceLabel;
    private JTable transferTable;
    private DefaultTableModel tableModel;
    private TransactionController transactionController;

    public FundTransferPanel() {
        transactionController = new TransactionController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Fund Transfer");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 90));
        add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Transfer Details"));

        // Row 1: From Account
        formPanel.add(new JLabel("From Account:"));
        fromAccountField = new JTextField();
        fromAccountField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(fromAccountField);

        JButton checkFromBtn = createButton("Check Balance", new Color(30, 100, 200));
        formPanel.add(checkFromBtn);

        fromBalanceLabel = new JLabel("Balance: --");
        fromBalanceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        fromBalanceLabel.setForeground(new Color(0, 130, 80));
        formPanel.add(fromBalanceLabel);

        // Row 2: To Account
        formPanel.add(new JLabel("To Account:"));
        toAccountField = new JTextField();
        toAccountField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(toAccountField);

        JButton checkToBtn = createButton("Check Balance", new Color(30, 100, 200));
        formPanel.add(checkToBtn);

        toBalanceLabel = new JLabel("Balance: --");
        toBalanceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        toBalanceLabel.setForeground(new Color(0, 130, 80));
        formPanel.add(toBalanceLabel);

        // Row 3: Amount
        formPanel.add(new JLabel("Amount (TK):"));
        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(amountField);

        formPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(descriptionField);

        // Row 4: Transfer button
        JButton transferBtn = createButton("Transfer", new Color(0, 150, 100));
        transferBtn.setPreferredSize(new Dimension(200, 40));
        JButton clearBtn    = createButton("Clear",    new Color(120, 120, 120));

        formPanel.add(transferBtn);
        formPanel.add(clearBtn);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        // Row 5: Info box
        JLabel infoLabel = new JLabel(
                "  ℹ️  Transfer করার আগে দুটো account-ই Active থাকতে হবে।");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(240, 248, 255));
        formPanel.add(infoLabel);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        // Table — recent transfers দেখাবে
        String[] columns = {"From Account", "To Account", "Amount (TK)", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        transferTable = new JTable(tableModel);
        transferTable.setRowHeight(28);
        transferTable.setFont(new Font("Arial", Font.PLAIN, 13));
        transferTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        transferTable.setSelectionBackground(new Color(173, 216, 230));

        JScrollPane tableScroll = new JScrollPane(transferTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Recent Transfers"));

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Button Actions
        checkFromBtn.addActionListener(e -> checkFromBalance());
        checkToBtn.addActionListener(e -> checkToBalance());
        transferBtn.addActionListener(e -> doTransfer());
        clearBtn.addActionListener(e -> clearFields());
    }

    private void checkFromBalance() {
        String accNum = fromAccountField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "From Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BigDecimal balance = transactionController.getBalance(accNum);
        if (balance != null) {
            fromBalanceLabel.setText("Balance: TK " + balance);
        } else {
            fromBalanceLabel.setText("Balance: --");
            JOptionPane.showMessageDialog(this, "Account পাওয়া যায়নি!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkToBalance() {
        String accNum = toAccountField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "To Account number দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BigDecimal balance = transactionController.getBalance(accNum);
        if (balance != null) {
            toBalanceLabel.setText("Balance: TK " + balance);
        } else {
            toBalanceLabel.setText("Balance: --");
            JOptionPane.showMessageDialog(this, "Account পাওয়া যায়নি!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doTransfer() {
        String fromAcc   = fromAccountField.getText().trim();
        String toAcc     = toAccountField.getText().trim();
        String amountText = amountField.getText().trim();

        // Validation
        if (fromAcc.isEmpty() || toAcc.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "From Account, To Account এবং Amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fromAcc.equals(toAcc)) {
            JOptionPane.showMessageDialog(this,
                    "From এবং To Account একই হতে পারবে না!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirm dialog
            int confirm = JOptionPane.showConfirmDialog(this,
                    fromAcc + " থেকে " + toAcc + " তে TK " + amount + " transfer করবে?",
                    "Confirm Transfer", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String result = transactionController.transfer(fromAcc, toAcc, amount);
                if ("SUCCESS".equals(result)) {
                    JOptionPane.showMessageDialog(this,
                            "TK " + amount + " সফলভাবে Transfer হয়েছে!");

                    // Table এ যোগ করা
                    tableModel.insertRow(0, new Object[]{
                            fromAcc, toAcc, amount, new java.util.Date()
                    });

                    // Balance update
                    checkFromBalance();
                    checkToBalance();
                    amountField.setText("");
                    descriptionField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        fromAccountField.setText("");
        toAccountField.setText("");
        amountField.setText("");
        descriptionField.setText("");
        fromBalanceLabel.setText("Balance: --");
        toBalanceLabel.setText("Balance: --");
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