package view;

import controller.AccountController;
import controller.CustomerController;
import model.Account;
import model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AccountPanel extends JPanel {

    private JComboBox<String> customerCombo;
    private JComboBox<String> accountTypeCombo;
    private JTextField initialDepositField, searchField;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private AccountController accountController;
    private CustomerController customerController;
    private List<Customer> customerList;

    public AccountPanel() {
        accountController = new AccountController();
        customerController = new CustomerController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        loadAccounts();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Account Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 90));
        add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Account"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Customer dropdown
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1;
        customerCombo = new JComboBox<>();
        customerCombo.setPreferredSize(new Dimension(200, 30));
        loadCustomerCombo();
        formPanel.add(customerCombo, gbc);

        // Account Type
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Account Type:"), gbc);
        gbc.gridx = 3;
        accountTypeCombo = new JComboBox<>(new String[]{"Savings", "Current", "Fixed"});
        accountTypeCombo.setPreferredSize(new Dimension(150, 30));
        formPanel.add(accountTypeCombo, gbc);

        // Initial Deposit
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Initial Deposit (৳):"), gbc);
        gbc.gridx = 1;
        initialDepositField = new JTextField(20);
        formPanel.add(initialDepositField, gbc);

        // Search field
        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Search Account:"), gbc);
        gbc.gridx = 3;
        searchField = new JTextField(20);
        formPanel.add(searchField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton createBtn  = createButton("Create Account", new Color(0, 150, 100));
        JButton searchBtn  = createButton("Search",         new Color(30, 100, 200));
        JButton refreshBtn = createButton("Refresh",        new Color(120, 120, 120));
        JButton freezeBtn  = createButton("Freeze/Unfreeze",new Color(200, 130, 0));

        btnPanel.add(createBtn);
        btnPanel.add(searchBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(freezeBtn);
        formPanel.add(btnPanel, gbc);

        // Table
        String[] columns = {"Account No", "Customer Name", "Type", "Balance (৳)", "Status", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        accountTable = new JTable(tableModel);
        accountTable.setRowHeight(28);
        accountTable.setFont(new Font("Arial", Font.PLAIN, 13));
        accountTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        accountTable.setSelectionBackground(new Color(173, 216, 230));

        // Balance column রঙ
        accountTable.getColumnModel().getColumn(3).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(value != null ? "৳ " + value.toString() : "");
                    lbl.setForeground(new Color(0, 130, 80));
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane tableScroll = new JScrollPane(accountTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Account List"));

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Button Actions
        createBtn.addActionListener(e -> createAccount());
        searchBtn.addActionListener(e -> searchAccount());
        refreshBtn.addActionListener(e -> loadAccounts());
        freezeBtn.addActionListener(e -> freezeAccount());
    }

    private void loadCustomerCombo() {
        customerList = customerController.getAllCustomers();
        customerCombo.removeAllItems();
        customerCombo.addItem("-- Select Customer --");
        for (Customer c : customerList) {
            customerCombo.addItem(c.getId() + " - " + c.getFullName());
        }
    }

    private void loadAccounts() {
        tableModel.setRowCount(0);
        List<Account> list = accountController.getAllAccounts();
        for (Account a : list) {
            tableModel.addRow(new Object[]{
                    a.getAccountNumber(),
                    a.getCustomerName(),
                    a.getAccountType(),
                    a.getBalance(),
                    a.getStatus(),
                    a.getCreatedAt()
            });
        }
    }

    private void createAccount() {
        if (customerCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Customer select করো!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String depositText = initialDepositField.getText().trim();
        if (depositText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Initial deposit দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            BigDecimal deposit = new BigDecimal(depositText);
            if (deposit.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Deposit amount 0 এর বেশি হতে হবে!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Selected customer এর ID বের করা
            String selected = (String) customerCombo.getSelectedItem();
            int customerId = Integer.parseInt(selected.split(" - ")[0]);
            String accountType = (String) accountTypeCombo.getSelectedItem();

            if (accountController.createAccount(customerId, accountType, deposit)) {
                JOptionPane.showMessageDialog(this, "Account সফলভাবে তৈরি হয়েছে!");
                initialDepositField.setText("");
                customerCombo.setSelectedIndex(0);
                loadAccounts();
                loadCustomerCombo();
            } else {
                JOptionPane.showMessageDialog(this, "Account তৈরি failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক amount দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchAccount() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAccounts();
            return;
        }
        tableModel.setRowCount(0);
        List<Account> list = accountController.getAllAccounts();
        for (Account a : list) {
            if (a.getAccountNumber().contains(keyword) ||
                    a.getCustomerName().toLowerCase().contains(keyword.toLowerCase())) {
                tableModel.addRow(new Object[]{
                        a.getAccountNumber(),
                        a.getCustomerName(),
                        a.getAccountType(),
                        a.getBalance(),
                        a.getStatus(),
                        a.getCreatedAt()
                });
            }
        }
    }

    private void freezeAccount() {
        int row = accountTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "আগে table থেকে একটা account select করো!");
            return;
        }
        String accNum = (String) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 4);
        String newStatus = status.equals("Active") ? "Frozen" : "Active";

        int confirm = JOptionPane.showConfirmDialog(this,
                accNum + " account টা " + newStatus + " করবে?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (accountController.updateStatus(accNum, newStatus)) {
                JOptionPane.showMessageDialog(this, "Account " + newStatus + " হয়েছে!");
                loadAccounts();
            }
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