package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:hotel.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        String sql = "CREATE TABLE IF NOT EXISTS rooms (" +
                     "number INTEGER PRIMARY KEY," +
                     "type TEXT NOT NULL," +
                     "price REAL NOT NULL," +
                     "booked BOOLEAN NOT NULL," +
                     "balcony BOOLEAN" +  
                     ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }
}
