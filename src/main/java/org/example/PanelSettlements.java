package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class PanelSettlements extends JPanel {

    // Допоміжні класи для зберігання об'єктів у випадаючих списках (ComboBox)
    class ClientItem {
        int id; String info;
        public ClientItem(int id, String info) { this.id = id; this.info = info; }
        @Override public String toString() { return info; }
    }

    class RoomItem {
        int number; double price;
        public RoomItem(int number, double price) { this.number = number; this.price = price; }
        @Override public String toString() { return "Кімната " + number + " (" + price + " грн/доба)"; }
    }

    // Компоненти форми
    private JComboBox<ClientItem> cbClients;
    private JComboBox<RoomItem> cbRooms;
    private JSpinner checkInSpinner;
    private JSpinner checkOutSpinner;
    private JCheckBox chkBreakfast, chkLaundry, chkTowels;
    private JLabel lblTotalCost;
    private JTextField tfPayment;
    private JButton btnSave, btnRefresh;

    public PanelSettlements() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Оформлення нового поселення", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formContainer = new JPanel(new GridBagLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 15, 20));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));


        cbClients = new JComboBox<>();
        cbRooms = new JComboBox<>();

        checkInSpinner = new JSpinner(new SpinnerDateModel());
        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));

        checkOutSpinner = new JSpinner(new SpinnerDateModel());
        checkOutSpinner.setValue(new Date(System.currentTimeMillis() + 86400000L));
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd"));

        JPanel servicesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chkBreakfast = new JCheckBox("Сніданок (150₴) ");
        chkLaundry = new JCheckBox("Прання (100₴) ");
        chkTowels = new JCheckBox("Рушники (50₴)");
        servicesPanel.add(chkBreakfast);
        servicesPanel.add(chkLaundry);
        servicesPanel.add(chkTowels);

        lblTotalCost = new JLabel("0.00 грн");
        lblTotalCost.setFont(new Font("Arial", Font.BOLD, 20));
        lblTotalCost.setForeground(new Color(0, 102, 51));

        tfPayment = new JTextField("0");
        tfPayment.setFont(new Font("Arial", Font.PLAIN, 16));

        formPanel.add(new JLabel("Оберіть клієнта:")); formPanel.add(cbClients);
        formPanel.add(new JLabel("Вільна кімната:")); formPanel.add(cbRooms);
        formPanel.add(new JLabel("Дата заїзду:")); formPanel.add(checkInSpinner);
        formPanel.add(new JLabel("Дата виїзду:")); formPanel.add(checkOutSpinner);
        formPanel.add(new JLabel("Додаткові послуги:")); formPanel.add(servicesPanel);
        formPanel.add(new JLabel("ЗАГАЛЬНА ВАРТІСТЬ:")); formPanel.add(lblTotalCost);
        formPanel.add(new JLabel("Внесена сума (Оплата):")); formPanel.add(tfPayment);

        formContainer.add(formPanel);
        add(formContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnSave = new JButton("Оформити поселення");
        btnSave.setFont(new Font("Arial", Font.BOLD, 16));
        btnSave.setPreferredSize(new Dimension(250, 45));
        btnSave.setBackground(new Color(51, 153, 255));
        btnSave.setForeground(Color.WHITE);

        btnRefresh = new JButton("Оновити списки");
        btnRefresh.setPreferredSize(new Dimension(150, 45));

        bottomPanel.add(btnSave);
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        initLogic();
        refreshData();
    }

    private void initLogic() {

        Runnable recalculate = () -> {
            try {
                Date inDate = (Date) checkInSpinner.getValue();
                Date outDate = (Date) checkOutSpinner.getValue();

                LocalDate inLocal = inDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate outLocal = outDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                long days = ChronoUnit.DAYS.between(inLocal, outLocal);
                if (days < 1) days = 1; // Мінімум 1 доба

                RoomItem selectedRoom = (RoomItem) cbRooms.getSelectedItem();
                double roomPrice = (selectedRoom != null) ? selectedRoom.price : 0;

                double total = days * roomPrice;
                if (chkBreakfast.isSelected()) total += (150 * days);
                if (chkLaundry.isSelected()) total += 100;
                if (chkTowels.isSelected()) total += 50;

                lblTotalCost.setText(String.format("%.2f", total).replace(",", "."));
            } catch (Exception ex) {}
        };

        cbRooms.addActionListener(e -> recalculate.run());
        checkInSpinner.addChangeListener(e -> recalculate.run());
        checkOutSpinner.addChangeListener(e -> recalculate.run());
        chkBreakfast.addActionListener(e -> recalculate.run());
        chkLaundry.addActionListener(e -> recalculate.run());
        chkTowels.addActionListener(e -> recalculate.run());

        btnSave.addActionListener(e -> {
            ClientItem client = (ClientItem) cbClients.getSelectedItem();
            RoomItem room = (RoomItem) cbRooms.getSelectedItem();

            if (client == null || room == null) {
                JOptionPane.showMessageDialog(this, "Немає вільних кімнат або клієнтів у базі!");
                return;
            }

            try {
                double totalCost = Double.parseDouble(lblTotalCost.getText());
                double payment = Double.parseDouble(tfPayment.getText().trim());
                String paymentStatus = (payment >= totalCost) ? "Оплачено" : "Не оплачено";

                java.sql.Date sqlIn = new java.sql.Date(((Date) checkInSpinner.getValue()).getTime());
                java.sql.Date sqlOut = new java.sql.Date(((Date) checkOutSpinner.getValue()).getTime());

                try (Connection conn = DBConnection.getConnection()) {
                    // 1. Записуємо нове поселення
                    String sql = "INSERT INTO Settlement (room_number, id_clients, check_in_date, check_out_date, total_cost, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, room.number);
                    pstmt.setInt(2, client.id);
                    pstmt.setDate(3, sqlIn);
                    pstmt.setDate(4, sqlOut);
                    pstmt.setDouble(5, totalCost);
                    pstmt.setString(6, paymentStatus);
                    pstmt.executeUpdate();

                    String updateRoom = "UPDATE Rooms SET status='Зайнято' WHERE room_number=?";
                    PreparedStatement pstmtRoom = conn.prepareStatement(updateRoom);
                    pstmtRoom.setInt(1, room.number);
                    pstmtRoom.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Поселення успішно оформлено!\n" +
                            "Кімната №" + room.number + " тепер зайнята.\n" +
                            "Статус оплати: " + paymentStatus);

                    // Очищуємо форму
                    tfPayment.setText("0");
                    chkBreakfast.setSelected(false);
                    chkLaundry.setSelected(false);
                    chkTowels.setSelected(false);
                    refreshData(); // Оновлюємо списки (зайнята кімната зникне)
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка при збереженні: Введіть коректну суму оплати.");
            }
        });

        btnRefresh.addActionListener(e -> refreshData());
    }


    private void refreshData() {
        cbClients.removeAllItems();
        cbRooms.removeAllItems();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {


            ResultSet rsClients = stmt.executeQuery("SELECT id_clients, passport_data FROM Clients");
            while (rsClients.next()) {
                cbClients.addItem(new ClientItem(rsClients.getInt("id_clients"), "Паспорт: " + rsClients.getString("passport_data")));
            }


            ResultSet rsRooms = stmt.executeQuery("SELECT room_number, price_per_night FROM Rooms WHERE status = 'Вільно'");
            while (rsRooms.next()) {
                cbRooms.addItem(new RoomItem(rsRooms.getInt("room_number"), rsRooms.getDouble("price_per_night")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}