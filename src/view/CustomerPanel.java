package view;

import controller.CustomerController;
import model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends JPanel {

    private JTextField nameField, emailField, phoneField, nidField;
    private JTextArea addressArea;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private CustomerController customerController;
    private int selectedCustomerId = -1;

    public CustomerPanel() {
        customerController = new CustomerController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        loadCustomers();
    }

    private void initUI() {
        // ── Title ──
        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 90));
        add(title, BorderLayout.NORTH);

        // ── Form Panel ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);

        // NID
        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("NID:"), gbc);
        gbc.gridx = 3;
        nidField = new JTextField(20);
        formPanel.add(nidField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        formPanel.add(new JScrollPane(addressArea), gbc);

        // ── Buttons ──
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton addBtn    = createButton("Add Customer",    new Color(0, 150, 100));
        JButton updateBtn = createButton("Update",          new Color(30, 100, 200));
        JButton deleteBtn = createButton("Delete",          new Color(200, 50, 50));
        JButton clearBtn  = createButton("Clear",           new Color(120, 120, 120));

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel, gbc);

        // ── Table ──
        String[] columns = {"ID", "Full Name", "Email", "Phone", "NID", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(28);
        customerTable.setFont(new Font("Arial", Font.PLAIN, 13));
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        customerTable.setSelectionBackground(new Color(173, 216, 230));
        JScrollPane tableScroll = new JScrollPane(customerTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Customer List"));

        // ── Layout ──
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ── Button Actions ──
        addBtn.addActionListener(e -> addCustomer());
        updateBtn.addActionListener(e -> updateCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        clearBtn.addActionListener(e -> clearFields());

        // Table row select করলে form-এ data আসবে
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });
    }

    // ── Helpers ──
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

    private void loadCustomers() {
        tableModel.setRowCount(0);
        List<Customer> list = customerController.getAllCustomers();
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getFullName(), c.getEmail(),
                    c.getPhone(), c.getNid(), c.getAddress()
            });
        }
    }

    private void addCustomer() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name দিন!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Customer c = new Customer();
        c.setFullName(nameField.getText().trim());
        c.setEmail(emailField.getText().trim());
        c.setPhone(phoneField.getText().trim());
        c.setNid(nidField.getText().trim());
        c.setAddress(addressArea.getText().trim());

        if (customerController.addCustomer(c)) {
            JOptionPane.showMessageDialog(this, "Customer সফলভাবে যোগ হয়েছে!");
            clearFields();
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(this, "Error! Email বা NID already আছে।", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        if (selectedCustomerId == -1) {
            JOptionPane.showMessageDialog(this, "আগে table থেকে একটা customer select করো!");
            return;
        }
        Customer c = new Customer();
        c.setId(selectedCustomerId);
        c.setFullName(nameField.getText().trim());
        c.setEmail(emailField.getText().trim());
        c.setPhone(phoneField.getText().trim());
        c.setNid(nidField.getText().trim());
        c.setAddress(addressArea.getText().trim());

        if (customerController.updateCustomer(c)) {
            JOptionPane.showMessageDialog(this, "Customer update হয়েছে!");
            clearFields();
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        if (selectedCustomerId == -1) {
            JOptionPane.showMessageDialog(this, "আগে table থেকে একটা customer select করো!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "এই customer delete করবে?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (customerController.deleteCustomer(selectedCustomerId)) {
                JOptionPane.showMessageDialog(this, "Customer delete হয়েছে!");
                clearFields();
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void fillFormFromTable() {
        int row = customerTable.getSelectedRow();
        if (row == -1) return;
        selectedCustomerId = (int) tableModel.getValueAt(row, 0);
        nameField.setText((String) tableModel.getValueAt(row, 1));
        emailField.setText((String) tableModel.getValueAt(row, 2));
        phoneField.setText((String) tableModel.getValueAt(row, 3));
        nidField.setText((String) tableModel.getValueAt(row, 4));
        addressArea.setText((String) tableModel.getValueAt(row, 5));
    }

    private void clearFields() {
        selectedCustomerId = -1;
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        nidField.setText("");
        addressArea.setText("");
        customerTable.clearSelection();
    }
}