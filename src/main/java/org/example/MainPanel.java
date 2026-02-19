package org.example;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;

public class MainPanel extends JPanel {

    public MainPanel(){
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu employeeMenu = new JMenu("Працівники");

        employeeMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                BorderLayout layout = (BorderLayout) getLayout();
                Component center = layout.getLayoutComponent(BorderLayout.CENTER);
                if (center != null) {
                    remove(center);
                }

                add(new EmployeePanel(), BorderLayout.CENTER);

                revalidate();
                repaint();

                employeeMenu.setSelected(false);
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }
        });


        menuBar.add(employeeMenu);
        add(menuBar, BorderLayout.NORTH);

    }





}
