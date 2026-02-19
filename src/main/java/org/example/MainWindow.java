package org.example;

import javax.swing.*;

public class MainWindow extends JFrame {
    private LoginPanel loginPanel;
    private MainPanel mainPanel;
    private EmployeePanel employeePanel;



    public MainWindow() {
        super("Хостел");

        loginPanel = new LoginPanel((Employee employee) -> {
            JOptionPane.showMessageDialog(this , "Ви ввійшли як користувач " + employee.getFullName());

            getContentPane().removeAll();

            add(new MainPanel());

            revalidate();
            repaint();
        });



        add(loginPanel);

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}