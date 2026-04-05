package view;

import controller.AdminController;
import model.Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminManagementFrame extends JFrame {

    private JTextField usernameField, fullNameField, passwordField;
    private JComboBox<String> roleCombo;
    private JTable adminTable;
    private DefaultTableModel tableModel;
    private AdminController adminController;

    public AdminManagementFrame(Admin currentAdmin) {
        adminController = new AdminController();
        setTitle("Admin Management");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
        loadAdmins();
        setVisible(true);
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Admin User Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(15, 40, 80));
        add(title, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Admin"));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        fullNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JTextField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Role:"));
        roleCombo = new JComboBox<>(new String[]{"Manager", "Teller"});
        formPanel.add(roleCombo);

        JButton addBtn   = createButton("Add Admin",  new Color(0, 150, 100));
        JButton clearBtn = createButton("Clear",      new Color(120, 120, 120));
        formPanel.add(addBtn);
        formPanel.add(clearBtn);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        // Table
        String[] columns = {"ID", "Username", "Full Name", "Role", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        adminTable = new JTable(tableModel);
        adminTable.setRowHeight(28);
        adminTable.setFont(new Font("Arial", Font.PLAIN, 13));
        adminTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        adminTable.setSelectionBackground(new Color(173, 216, 230));

        // Role column রঙ
        adminTable.getColumnModel().getColumn(3).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel(" " + (value != null ? value.toString() : ""));
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setOpaque(true);
                    switch (value != null ? value.toString() : "") {
                        case "Super Admin": lbl.setForeground(new Color(180, 50, 50));  break;
                        case "Manager":     lbl.setForeground(new Color(30, 130, 80));  break;
                        default:            lbl.setForeground(new Color(30, 100, 180)); break;
                    }
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane scroll = new JScrollPane(adminTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Admin List"));

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton activateBtn   = createButton("Activate",   new Color(0, 150, 100));
        JButton deactivateBtn = createButton("Deactivate", new Color(200, 50, 50));
        btnPanel.add(activateBtn);
        btnPanel.add(deactivateBtn);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(btnPanel, BorderLayout.NORTH);
        tablePanel.add(scroll,   BorderLayout.CENTER);

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(formPanel,  BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Actions
        addBtn.addActionListener(e -> addAdmin());
        clearBtn.addActionListener(e -> clearFields());

        activateBtn.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Admin select করো!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            if (adminController.updateStatus(id, "Active")) {
                JOptionPane.showMessageDialog(this, "Admin Activated!");
                loadAdmins();
            }
        });

        deactivateBtn.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Admin select করো!");
                return;
            }
            int id   = (int) tableModel.getValueAt(row, 0);
            String role = (String) tableModel.getValueAt(row, 3);
            if ("Super Admin".equals(role)) {
                JOptionPane.showMessageDialog(this, "Super Admin কে deactivate করা যাবে না!");
                return;
            }
            if (adminController.updateStatus(id, "Inactive")) {
                JOptionPane.showMessageDialog(this, "Admin Deactivated!");
                loadAdmins();
            }
        });
    }

    private void loadAdmins() {
        tableModel.setRowCount(0);
        List<Admin> list = adminController.getAllAdmins();
        for (Admin a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(), a.getUsername(), a.getFullName(),
                    a.getRole(), a.getStatus()
            });
        }
    }

    private void addAdmin() {
        if (usernameField.getText().trim().isEmpty() ||
                fullNameField.getText().trim().isEmpty() ||
                passwordField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "সব field পূরণ করো!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Admin a = new Admin();
        a.setUsername(usernameField.getText().trim());
        a.setFullName(fullNameField.getText().trim());
        a.setPassword(passwordField.getText().trim());
        a.setRole((String) roleCombo.getSelectedItem());

        if (adminController.addAdmin(a)) {
            JOptionPane.showMessageDialog(this, "Admin সফলভাবে যোগ হয়েছে!");
            clearFields();
            loadAdmins();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed! Username already আছে।", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        usernameField.setText("");
        fullNameField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 36));
        return btn;
    }
}