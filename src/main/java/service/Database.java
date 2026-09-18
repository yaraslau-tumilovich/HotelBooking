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

        String roomsSql = "CREATE TABLE IF NOT EXISTS rooms (" +
                "number INTEGER PRIMARY KEY," +
                "type TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "booked BOOLEAN NOT NULL," +
                "balcony BOOLEAN" +
                ");";

        String usersSql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL" +
                ");";

        try (Connection conn = connect();
            Statement stmt = conn.createStatement()) {

            stmt.execute(roomsSql);
            stmt.execute(usersSql);

            String bookingsSql = "CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "room_number INTEGER NOT NULL UNIQUE," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (room_number) REFERENCES rooms(number)" +
                    ");";

        stmt.execute(bookingsSql);

            String adminSql = "INSERT OR IGNORE INTO users " +
                    "(username, password, role) " +
                    "VALUES ('admin', 'admin123', 'ADMIN')";

            stmt.execute(adminSql);

            System.out.println("Database initialized successfully!");

} catch (SQLException e) {
            System.out.println(
                    "Error initializing database: " + e.getMessage()
            );
        }
    }
}