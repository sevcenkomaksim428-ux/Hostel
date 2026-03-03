package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel extends JPanel {

    public MainPanel() {
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();

        JMenu employeeMenu = new JMenu("Працівники");
        JMenu roomsMenu = new JMenu("Кімнати");
        JMenu clientsMenu = new JMenu("Клієнти");
        JMenu settlementsMenu = new JMenu("Поселення"); // <-- НОВИЙ ПУНКТ

        employeeMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPanel(new EmployeePanel());
            }
        });


        roomsMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPanel(new PanelRooms());
            }
        });


        clientsMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPanel(new PanelClients());
            }
        });

        settlementsMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPanel(new PanelSettlements());
            }
        });


        menuBar.add(employeeMenu);
        menuBar.add(roomsMenu);
        menuBar.add(clientsMenu);
        menuBar.add(settlementsMenu);


        add(menuBar, BorderLayout.NORTH);


        JLabel welcomeLabel = new JLabel("Вітаємо в системі! Оберіть розділ у меню зверху.", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(welcomeLabel, BorderLayout.CENTER);
    }

    private void openPanel(JPanel newPanel) {
        BorderLayout layout = (BorderLayout) getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);

        if (center != null) {
            remove(center);
        }


        add(newPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}