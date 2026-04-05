package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bank_db";
    private static final String USER = "root";       // তোমার MySQL username
    private static final String PASSWORD = "";   // তোমার MySQL password

    private static Connection connection = null;

    // Private constructor — কেউ object বানাতে পারবে না
    private DBConnection() {}

    // Singleton pattern — সবসময় একটাই connection থাকবে
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found! JAR file add করো।");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed! Password/URL check করো।");
            e.printStackTrace();
        }
        return connection;
    }

    // Connection বন্ধ করার জন্য
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}