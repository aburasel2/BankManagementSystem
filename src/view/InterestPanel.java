package view;

import controller.InterestController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class InterestPanel extends JPanel {

    private JTextField savingsRateField, currentRateField, fixedRateField;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private InterestController interestController;
    private JLabel statusLabel;

    public InterestPanel() {
        interestController = new InterestController();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
        loadRates();
        loadHistory();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Interest Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(15, 40, 80));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);

        // ── Top: Rate Settings + Apply ──
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        topPanel.setBackground(Color.WHITE);

        // Rate Settings Panel
        JPanel ratePanel = new JPanel(new GridLayout(4, 4, 8, 8));
        ratePanel.setBackground(Color.WHITE);
        ratePanel.setBorder(BorderFactory.createTitledBorder("Interest Rate Settings (Annual %)"));

        ratePanel.add(new JLabel("Savings Account:"));
        savingsRateField = new JTextField();
        savingsRateField.setFont(new Font("Arial", Font.PLAIN, 14));
        ratePanel.add(savingsRateField);
        ratePanel.add(new JLabel("% per year"));
        ratePanel.add(new JLabel(""));

        ratePanel.add(new JLabel("Current Account:"));
        currentRateField = new JTextField();
        currentRateField.setFont(new Font("Arial", Font.PLAIN, 14));
        ratePanel.add(currentRateField);
        ratePanel.add(new JLabel("% per year"));
        ratePanel.add(new JLabel(""));

        ratePanel.add(new JLabel("Fixed Deposit:"));
        fixedRateField = new JTextField();
        fixedRateField.setFont(new Font("Arial", Font.PLAIN, 14));
        ratePanel.add(fixedRateField);
        ratePanel.add(new JLabel("% per year"));
        ratePanel.add(new JLabel(""));

        JButton saveRateBtn = createButton("Save Rates", new Color(30, 100, 200));
        ratePanel.add(saveRateBtn);
        ratePanel.add(new JLabel(""));
        ratePanel.add(new JLabel(""));
        ratePanel.add(new JLabel(""));

        // Apply Interest Panel
        JPanel applyPanel = new JPanel(new GridBagLayout());
        applyPanel.setBackground(new Color(240, 248, 255));
        applyPanel.setBorder(BorderFactory.createTitledBorder("Apply Monthly Interest"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridx = 0;

        JLabel applyIcon = new JLabel("💰");
        applyIcon.setFont(new Font("Arial", Font.PLAIN, 40));
        gbc.gridy = 0;
        applyPanel.add(applyIcon, gbc);

        JLabel applyDesc = new JLabel("<html><center>সব Active Savings ও Fixed<br>account এ interest যোগ করো</center></html>");
        applyDesc.setFont(new Font("Arial", Font.PLAIN, 13));
        applyDesc.setForeground(new Color(60, 80, 120));
        applyDesc.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        applyPanel.add(applyDesc, gbc);

        JButton applyBtn = createButton("Apply Monthly Interest", new Color(0, 150, 100));
        applyBtn.setPreferredSize(new Dimension(200, 42));
        gbc.gridy = 2;
        applyPanel.add(applyBtn, gbc);

        statusLabel = new JLabel("  ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        applyPanel.add(statusLabel, gbc);

        JLabel noteLabel = new JLabel(
                "<html><center><i>একই মাসে দুইবার apply<br>করা যাবে না।</i></center></html>");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        noteLabel.setForeground(new Color(150, 150, 150));
        noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        applyPanel.add(noteLabel, gbc);

        topPanel.add(ratePanel);
        topPanel.add(applyPanel);

        // ── Bottom: History Table ──
        String[] columns = {"ID", "Account No", "Type", "Interest (TK)",
                "Balance Before", "Balance After", "Rate %", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.setSelectionBackground(new Color(173, 216, 230));

        // Interest column সবুজ
        historyTable.getColumnModel().getColumn(3).setCellRenderer(
                (table, value, isSelected, hasFocus, row, col) -> {
                    JLabel lbl = new JLabel("+ TK " + (value != null ? value : ""));
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setForeground(new Color(0, 130, 80));
                    lbl.setOpaque(true);
                    lbl.setBackground(isSelected ? new Color(173, 216, 230) : Color.WHITE);
                    return lbl;
                });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Interest History"));

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(scroll,   BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ── Actions ──
        saveRateBtn.addActionListener(e -> saveRates());
        applyBtn.addActionListener(e -> applyInterest());
    }

    private void loadRates() {
        Map<String, Double> rates = interestController.getInterestRates();
        savingsRateField.setText(String.valueOf(rates.getOrDefault("Savings", 6.0)));
        currentRateField.setText(String.valueOf(rates.getOrDefault("Current", 2.0)));
        fixedRateField.setText(String.valueOf(rates.getOrDefault("Fixed", 10.0)));
    }

    private void saveRates() {
        try {
            double savings = Double.parseDouble(savingsRateField.getText().trim());
            double current = Double.parseDouble(currentRateField.getText().trim());
            double fixed   = Double.parseDouble(fixedRateField.getText().trim());

            if (savings < 0 || current < 0 || fixed < 0) {
                JOptionPane.showMessageDialog(this, "Rate 0 এর কম হতে পারবে না!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            interestController.updateRate("Savings", savings);
            interestController.updateRate("Current", current);
            interestController.updateRate("Fixed",   fixed);

            JOptionPane.showMessageDialog(this, "Interest rates সফলভাবে update হয়েছে!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "সঠিক সংখ্যা দিন!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyInterest() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "সব Active Savings ও Fixed account এ এই মাসের interest apply করবে?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String result = interestController.applyMonthlyInterest();

        if (result.startsWith("SUCCESS")) {
            String[] parts = result.split(":");
            int success = Integer.parseInt(parts[1]);
            int skipped = Integer.parseInt(parts[2]);
            double total = Double.parseDouble(parts[3]);

            statusLabel.setForeground(new Color(0, 130, 80));
            statusLabel.setText(String.format(
                    "✔ %d accounts এ TK %.2f interest দেওয়া হয়েছে", success, total));

            JOptionPane.showMessageDialog(this,
                    String.format("সফল!\n\n%d account এ interest দেওয়া হয়েছে\n" +
                                    "%d account skip হয়েছে (already done)\nমোট interest: TK %.2f",
                            success, skipped, total));
            loadHistory();
        } else {
            statusLabel.setForeground(new Color(200, 50, 50));
            statusLabel.setText("Error: " + result);
            JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        List<Map<String, Object>> list = interestController.getInterestHistory();
        for (Map<String, Object> row : list) {
            tableModel.addRow(new Object[]{
                    row.get("id"),
                    row.get("account_number"),
                    row.get("account_type"),
                    row.get("interest_amount"),
                    row.get("balance_before"),
                    row.get("balance_after"),
                    row.get("rate"),
                    row.get("applied_date")
            });
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