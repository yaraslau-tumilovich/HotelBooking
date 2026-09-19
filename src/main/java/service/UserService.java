package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Role;
import model.User;

public class UserService {

    public boolean register(String username, String password) {

        String sql = "INSERT INTO users (username, password, role) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, "CLIENT");

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println(
                    "Registration error: " + e.getMessage());

            return false;
        }
    }

    public boolean deleteAccount(User user) {

        String findUserSql = "SELECT id FROM users WHERE username = ?";

        String deleteBookingsSql = "DELETE FROM bookings WHERE user_id = ?";

        String deleteUserSql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Database.connect()) {

            conn.setAutoCommit(false);

            try {

                int userId;

                try (PreparedStatement stmt = conn.prepareStatement(findUserSql)) {

                    stmt.setString(1, user.getUsername());

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        System.out.println("User not found.");
                        conn.rollback();
                        return false;
                    }

                    userId = rs.getInt("id");
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE rooms SET booked = false " +
                                "WHERE number IN " +
                                "(SELECT room_number FROM bookings " +
                                "WHERE user_id = ?)")) {

                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteBookingsSql)) {

                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {

                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }

                conn.commit();

                System.out.println(
                        "Account deleted successfully.");

                return true;

            } catch (SQLException e) {

                conn.rollback();

                System.out.println(
                        "Error deleting account: " +
                                e.getMessage());

                return false;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database error: " +
                            e.getMessage());

            return false;
        }
    }

    public void showAllUsers() {

        String sql = "SELECT id, username, role " +
                "FROM users " +
                "ORDER BY id";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n=== ALL ACCOUNTS ===");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        "ID: " + rs.getInt("id"));

                System.out.println(
                        "Username: " + rs.getString("username"));

                System.out.println(
                        "Role: " + rs.getString("role"));

                System.out.println("--------------------");
            }

            if (!found) {
                System.out.println("No accounts found.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error fetching accounts: " +
                            e.getMessage());
        }
    }

    public void showUserBookings(String username) {

        String sql = "SELECT b.id, b.check_in, b.check_out, " +
                "r.number, r.type, r.price, r.balcony " +
                "FROM users u " +
                "JOIN bookings b ON u.id = b.user_id " +
                "JOIN rooms r ON b.room_number = r.number " +
                "WHERE u.username = ?";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            System.out.println(
                    "\n=== BOOKINGS OF " + username + " ===");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        "Booking ID: " + rs.getInt("id"));

                System.out.println(
                        "Check-in: " + rs.getString("check_in"));

                System.out.println(
                        "Check-out: " + rs.getString("check_out"));

                System.out.println(
                        "Room: " + rs.getInt("number"));

                System.out.println(
                        "Type: " + rs.getString("type"));

                System.out.println(
                        "Price: $" + rs.getDouble("price") +
                                " / night");

                if ("Deluxe".equals(rs.getString("type"))) {

                    System.out.println(
                            "Balcony: " +
                                    (rs.getBoolean("balcony")
                                            ? "Yes"
                                            : "No"));
                }

                System.out.println(
                        "--------------------");
            }

            if (!found) {

                System.out.println(
                        "This user has no bookings.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error fetching user bookings: " +
                            e.getMessage());
        }
    }

    public boolean deleteUserByUsername(String username) {

        String findUserSql = "SELECT id, role FROM users WHERE username = ?";

        String freeRoomsSql = "UPDATE rooms SET booked = false " +
                "WHERE number IN " +
                "(SELECT room_number FROM bookings " +
                "WHERE user_id = ?)";

        String deleteBookingsSql = "DELETE FROM bookings WHERE user_id = ?";

        String deleteUserSql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Database.connect()) {

            conn.setAutoCommit(false);

            try {

                int userId;
                String role;

                // Find user
                try (PreparedStatement stmt = conn.prepareStatement(findUserSql)) {

                    stmt.setString(1, username);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {

                        System.out.println(
                                "User not found.");

                        conn.rollback();
                        return false;
                    }

                    userId = rs.getInt("id");
                    role = rs.getString("role");
                }

                // Do not allow deleting admin
                if ("ADMIN".equals(role)) {

                    System.out.println(
                            "You cannot delete an ADMIN account.");

                    conn.rollback();
                    return false;
                }

                // Free user's rooms
                try (PreparedStatement stmt = conn.prepareStatement(freeRoomsSql)) {

                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }

                // Delete bookings
                try (PreparedStatement stmt = conn.prepareStatement(deleteBookingsSql)) {

                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }

                // Delete user
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {

                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }

                conn.commit();

                System.out.println(
                        "User '" + username +
                                "' deleted successfully.");

                return true;

            } catch (SQLException e) {

                conn.rollback();

                System.out.println(
                        "Error deleting user: " +
                                e.getMessage());

                return false;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database error: " +
                            e.getMessage());

            return false;
        }
    }

    public User login(String username, String password) {

        String sql = "SELECT * FROM users " +
                "WHERE username = ? AND password = ?";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet result = stmt.executeQuery();

            if (result.next()) {

                String roleString = result.getString("role");

                Role role = Role.valueOf(roleString);

                return new User(
                        result.getString("username"),
                        result.getString("password"),
                        role);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Login error: " + e.getMessage());
        }

        return null;
    }
}