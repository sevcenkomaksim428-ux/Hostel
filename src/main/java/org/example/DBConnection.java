package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final static String url = "jdbc:mysql://localhost:3306/hostel?useSSL=false&serverTimezone=UTC";
    private final static String user = "root";
    private final static String password = "1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}