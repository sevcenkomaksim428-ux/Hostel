package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PanelClients extends JPanel {
    private JLabel titleLabel;
    private JTable table;
    private JButton addButton, updateButton, deleteButton;
    private DefaultTableModel tableModel;

    public PanelClients() {
        setLayout(new BorderLayout());

        titleLabel = new JLabel("Управління клієнтами", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"ID", "Паспортні дані", "Телефон", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Додати");
        updateButton = new JButton("Оновити");
        deleteButton = new JButton("Видалити");

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        initListeners();
        loadData();
    }

    private void initListeners() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = table.getSelectedRow() != -1;
                updateButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        addButton.addActionListener(e -> {
            String[] data = showClientDialog("Додати клієнта", "", "", "");
            if (data != null) {
                String sql = "INSERT INTO Clients (passport_data, phone, email) VALUES (?, ?, ?)";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, data[0]);
                    pstmt.setString(2, data[1]);
                    pstmt.setString(3, data[2]);
                    pstmt.executeUpdate();
                    loadData();
                    JOptionPane.showMessageDialog(this, "Клієнта додано!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка БД: " + ex.getMessage());
                }
            }
        });

        updateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String pass = tableModel.getValueAt(row, 1).toString();
            String phone = tableModel.getValueAt(row, 2).toString();
            String email = tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "";

            String[] data = showClientDialog("Редагувати клієнта", pass, phone, email);
            if (data != null) {
                String sql = "UPDATE Clients SET passport_data=?, phone=?, email=? WHERE id_clients=?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, data[0]);
                    pstmt.setString(2, data[1]);
                    pstmt.setString(3, data[2]);
                    pstmt.setInt(4, id);
                    pstmt.executeUpdate();
                    loadData();
                    JOptionPane.showMessageDialog(this, "Дані оновлено!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка БД: " + ex.getMessage());
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Видалити клієнта (ID: " + id + ")?", "Увага", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Clients WHERE id_clients=?")) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage());
                }
            }
        });
    }

    private String[] showClientDialog(String title, String pass, String phone, String email) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JTextField tfPass = new JTextField(pass, 15);
        JTextField tfPhone = new JTextField(phone, 15);
        JTextField tfEmail = new JTextField(email, 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Паспортні дані:")); panel.add(tfPass);
        panel.add(new JLabel("Телефон:")); panel.add(tfPhone);
        panel.add(new JLabel("Email:")); panel.add(tfEmail);

        dialog.add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Зберегти");
        JButton btnCancel = new JButton("Скасувати");

        final String[][] result = {null};

        btnSave.addActionListener(e -> {
            if (tfPass.getText().trim().isEmpty() || tfPhone.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Паспорт та Телефон обов'язкові!");
                return;
            }
            result[0] = new String[]{tfPass.getText().trim(), tfPhone.getText().trim(), tfEmail.getText().trim()};
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnSave); btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.pack(); dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
        return result[0];
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Clients")) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_clients"));
                row.add(rs.getString("passport_data"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("email"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}