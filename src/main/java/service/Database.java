package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.mindrot.jbcrypt.BCrypt;

public class Database {

        private static final String DB_URL = "jdbc:sqlite:hotel.db";

        public static Connection connect() throws SQLException {
                Connection conn = DriverManager.getConnection(DB_URL);
                try (Statement st = conn.createStatement()) {
                        st.execute("PRAGMA foreign_keys = ON");
                }
                return conn;
        }

        public static void initialize() {

                String roomsSql = "CREATE TABLE IF NOT EXISTS rooms (" +
                                "number INTEGER PRIMARY KEY," +
                                "type TEXT NOT NULL," +
                                "price REAL NOT NULL," +
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
                                        "room_number INTEGER NOT NULL," +
                                        "check_in TEXT NOT NULL," +
                                        "check_out TEXT NOT NULL," +
                                        "FOREIGN KEY (user_id) REFERENCES users(id)," +
                                        "FOREIGN KEY (room_number) REFERENCES rooms(number)" +
                                        ");";

                        stmt.execute(bookingsSql);

                        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");

                        String adminSql = "INSERT OR IGNORE INTO users (username, password, role) " +
                                        "VALUES ('admin', ?, 'ADMIN')";

                        try (PreparedStatement ps = conn.prepareStatement(adminSql)) {
                                ps.setString(1, BCrypt.hashpw(adminPassword, BCrypt.gensalt()));
                                ps.executeUpdate();
                        }

                        System.out.println("Database initialized successfully!");

                } catch (SQLException e) {
                        System.out.println(
                                        "Error initializing database: " + e.getMessage());
                }
        }
}