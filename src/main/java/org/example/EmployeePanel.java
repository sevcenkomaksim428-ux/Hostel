package org.example;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class EmployeePanel extends JPanel {
    private JLabel employee;
    private JTable table;
    private JButton addButton;
    private JButton updateButton;
    private JButton delete;
    private DefaultTableModel tableModel;

    public EmployeePanel() {
        setLayout(new BorderLayout());

        // 1. Верх
        employee = new JLabel("Управління працівниками", SwingConstants.CENTER);
        employee.setFont(new Font("Arial", Font.BOLD, 18));
        add(employee, BorderLayout.NORTH);

        // 2. Таблиця
        String[] columns = {"ID", "ПІБ", "Посада", "Зарплата", "Логін", "Пароль"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Забороняємо редагувати клітинки напряму
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Додати");
        updateButton = new JButton("Оновити");
        delete = new JButton("Видалити");

        updateButton.setEnabled(false);
        delete.setEnabled(false);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(delete);

        add(buttonPanel, BorderLayout.SOUTH);

        initListeners();
        loadData();
    }

    private void initListeners() {
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean hasSelection = table.getSelectedRow() != -1;
                    updateButton.setEnabled(hasSelection);
                    delete.setEnabled(hasSelection);
                }
            }
        });

        addButton.addActionListener(e -> {
            String[] data = showEmployeeDialog("Додати працівника", "", "", "", "", "");

            if (data != null) {
                String sql = "INSERT INTO employee (full_name, role, salary, login, password) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, data[0]);
                    pstmt.setString(2, data[1]);
                    pstmt.setDouble(3, Double.parseDouble(data[2]));
                    pstmt.setString(4, data[3]);
                    pstmt.setString(5, data[4]);

                    pstmt.executeUpdate();
                    loadData();
                    JOptionPane.showMessageDialog(this, "Працівника додано!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Помилка: Зарплата має бути числом!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка БД: " + ex.getMessage());
                }
            }
        });

        updateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String currentName = tableModel.getValueAt(row, 1).toString();
            String currentRole = tableModel.getValueAt(row, 2).toString();
            String currentSalary = tableModel.getValueAt(row, 3).toString();
            String currentLogin = tableModel.getValueAt(row, 4).toString();
            String currentPass = tableModel.getValueAt(row, 5).toString();

            String[] newData = showEmployeeDialog("Редагувати працівника", currentName, currentRole, currentSalary, currentLogin, currentPass);

            if (newData != null) {
                String sql = "UPDATE employee SET full_name=?, role=?, salary=?, login=?, password=? WHERE id=?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, newData[0]);
                    pstmt.setString(2, newData[1]);
                    pstmt.setDouble(3, Double.parseDouble(newData[2]));
                    pstmt.setString(4, newData[3]);
                    pstmt.setString(5, newData[4]);
                    pstmt.setInt(6, id);

                    pstmt.executeUpdate();
                    loadData();
                    JOptionPane.showMessageDialog(this, "Дані оновлено!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Помилка: Зарплата має бути числом!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка БД: " + ex.getMessage());
                }
            }
        });

        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Ви впевнені, що хочете видалити працівника (ID: " + id + ")?", "Підтвердження", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employee WHERE id=?")) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage());
                }
            }
        });
    }

    private String[] showEmployeeDialog(String title, String name, String role, String salary, String login, String pass) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JTextField tfName = new JTextField(name, 15);
        JTextField tfRole = new JTextField(role, 15);
        JTextField tfSalary = new JTextField(salary, 15);
        JTextField tfLogin = new JTextField(login, 15);
        JTextField tfPass = new JTextField(pass, 15);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.add(new JLabel("ПІБ:")); formPanel.add(tfName);
        formPanel.add(new JLabel("Посада:")); formPanel.add(tfRole);
        formPanel.add(new JLabel("Зарплата:")); formPanel.add(tfSalary);
        formPanel.add(new JLabel("Логін:")); formPanel.add(tfLogin);
        formPanel.add(new JLabel("Пароль:")); formPanel.add(tfPass);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Зберегти");
        JButton btnCancel = new JButton("Скасувати");

        final String[][] result = {null};

        btnSave.addActionListener(e -> {
            if (tfName.getText().trim().isEmpty() || tfSalary.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "ПІБ та Зарплата є обов'язковими!");
                return;
            }
            result[0] = new String[]{
                    tfName.getText().trim(),
                    tfRole.getText().trim(),
                    tfSalary.getText().trim(),
                    tfLogin.getText().trim(),
                    tfPass.getText().trim()
            };
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setVisible(true);

        return result[0];
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employee")) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("role"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getString("login"));
                row.add(rs.getString("password"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}