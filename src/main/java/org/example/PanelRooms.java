package org.example;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PanelRooms extends JPanel {
    private JLabel titleLabel;
    private JTable table;
    private JButton addButton, updateButton, deleteButton;
    private DefaultTableModel tableModel;

    public PanelRooms() {
        setLayout(new BorderLayout());

        titleLabel = new JLabel("Управління кімнатами", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Номер", "Тип", "Всього місць", "Зайнято", "Ціна за добу", "Статус"};
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
            Object[] data = showRoomDialog("Додати кімнату", "", "Стандарт", "", "0", "", "Вільно", false);
            if (data != null) {
                String sql = "INSERT INTO Rooms (room_number, room_type, total_beds, occupied_beds, price_per_night, status) VALUES (?, ?, ?, ?, ?, ?)";
                executeRoomQuery(sql, data, true);
            }
        });

        updateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            String roomNum = tableModel.getValueAt(row, 0).toString();
            String type = tableModel.getValueAt(row, 1).toString();
            String totalBeds = tableModel.getValueAt(row, 2).toString();
            String occBeds = tableModel.getValueAt(row, 3).toString();
            String price = tableModel.getValueAt(row, 4).toString();
            String status = tableModel.getValueAt(row, 5).toString();

            // isUpdate = true (блокуємо поле з номером кімнати, бо це Primary Key)
            Object[] data = showRoomDialog("Редагувати кімнату", roomNum, type, totalBeds, occBeds, price, status, true);

            if (data != null) {
                String sql = "UPDATE Rooms SET room_type=?, total_beds=?, occupied_beds=?, price_per_night=?, status=? WHERE room_number=?";
                executeRoomQuery(sql, data, false);
            }
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            int roomNum = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Видалити кімнату №" + roomNum + "?", "Увага", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Rooms WHERE room_number=?")) {
                    pstmt.setInt(1, roomNum);
                    pstmt.executeUpdate();
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка видалення: " + ex.getMessage());
                }
            }
        });
    }

    private void executeRoomQuery(String sql, Object[] data, boolean isInsert) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (isInsert) {
                pstmt.setInt(1, Integer.parseInt(data[0].toString()));
                pstmt.setString(2, data[1].toString());
                pstmt.setInt(3, Integer.parseInt(data[2].toString()));
                pstmt.setInt(4, Integer.parseInt(data[3].toString()));
                pstmt.setDouble(5, Double.parseDouble(data[4].toString()));
                pstmt.setString(6, data[5].toString());
            } else {
                pstmt.setString(1, data[1].toString());
                pstmt.setInt(2, Integer.parseInt(data[2].toString()));
                pstmt.setInt(3, Integer.parseInt(data[3].toString()));
                pstmt.setDouble(4, Double.parseDouble(data[4].toString()));
                pstmt.setString(5, data[5].toString());
                pstmt.setInt(6, Integer.parseInt(data[0].toString())); // WHERE room_number=?
            }
            pstmt.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Операція успішна!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка БД: " + ex.getMessage());
        }
    }

    private Object[] showRoomDialog(String title, String num, String type, String tBeds, String oBeds, String price, String status, boolean isUpdate) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JTextField tfNum = new JTextField(num, 15);
        if (isUpdate) tfNum.setEnabled(false); // Не можна міняти номер існуючої кімнати

        JComboBox<String> cbType = new JComboBox<>(new String[]{"Стандарт", "Люкс"});
        cbType.setSelectedItem(type);

        JTextField tfTotalBeds = new JTextField(tBeds, 15);
        JTextField tfOccBeds = new JTextField(oBeds, 15);
        JTextField tfPrice = new JTextField(price, 15);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Вільно", "Зайнято", "Технічне обслуговування"});
        cbStatus.setSelectedItem(status);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Номер кімнати:")); panel.add(tfNum);
        panel.add(new JLabel("Тип:")); panel.add(cbType);
        panel.add(new JLabel("Всього місць:")); panel.add(tfTotalBeds);
        panel.add(new JLabel("Зайнято місць:")); panel.add(tfOccBeds);
        panel.add(new JLabel("Ціна за добу:")); panel.add(tfPrice);
        panel.add(new JLabel("Статус:")); panel.add(cbStatus);

        dialog.add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Зберегти");
        JButton btnCancel = new JButton("Скасувати");

        final Object[][] result = {null};

        btnSave.addActionListener(e -> {
            try {
                // Валідація чисел
                Integer.parseInt(tfNum.getText().trim());
                Integer.parseInt(tfTotalBeds.getText().trim());
                Integer.parseInt(tfOccBeds.getText().trim());
                Double.parseDouble(tfPrice.getText().trim());

                result[0] = new Object[]{
                        tfNum.getText().trim(),
                        cbType.getSelectedItem(),
                        tfTotalBeds.getText().trim(),
                        tfOccBeds.getText().trim(),
                        tfPrice.getText().trim(),
                        cbStatus.getSelectedItem()
                };
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Номер, місця та ціна мають бути числами!");
            }
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
             ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms")) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("room_number"));
                row.add(rs.getString("room_type"));
                row.add(rs.getInt("total_beds"));
                row.add(rs.getInt("occupied_beds"));
                row.add(rs.getDouble("price_per_night"));
                row.add(rs.getString("status"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}