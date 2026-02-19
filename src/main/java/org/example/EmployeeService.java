package org.example;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeService {

    public static Employee login(String login, String password) {
        String request = "SELECT id, full_name, role, salary FROM employee WHERE login = ? AND password = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(request);
        ) {
            ps.setString(1, login);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int id = rs.getInt("id");
                String name = rs.getString("full_name");
                String role = rs.getString("role");
                int salary = rs.getInt("salary");

                return new Employee(id, name, role, salary, login, password);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Сталася помилка під час запиту! " + e.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Сталася помилка під час підключення! " + e.getMessage());
        }

        return null;
    }

}
