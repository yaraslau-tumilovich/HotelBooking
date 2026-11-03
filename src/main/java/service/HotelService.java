package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.DeluxeRoom;
import model.Role;
import model.Room;
import model.StandardRoom;
import model.User;

public class HotelService {

        public HotelService() {
                Database.initialize();
        }

        public void addRoom(Room room) {

                String sql = "INSERT INTO rooms(number,type,price,booked,balcony) " +
                                "VALUES(?,?,?,?,?)";

                try (Connection conn = Database.connect();
                                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setInt(1, room.getRoomNumber());

                        pstmt.setString(
                                        2,
                                        room instanceof DeluxeRoom
                                                        ? "Deluxe"
                                                        : "Standard");

                        pstmt.setDouble(3, room.getPricePerNight());

                        pstmt.setBoolean(4, false);

                        pstmt.setBoolean(
                                        5,
                                        room instanceof DeluxeRoom
                                                        && ((DeluxeRoom) room).hasBalcony());

                        pstmt.executeUpdate();

                        System.out.println(
                                        "Room " + room.getRoomNumber() +
                                                        " added successfully!");

                } catch (SQLException e) {

                        System.out.println(
                                        "Error adding room: " + e.getMessage());
                }
        }

        public void changeRoomPrice(int roomNumber, double newPrice) {

                if (newPrice <= 0) {
                        System.out.println("Price must be greater than 0.");
                        return;
                }

                String sql = "UPDATE rooms SET price = ? WHERE number = ?";

                try (Connection conn = Database.connect();
                                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setDouble(1, newPrice);
                        pstmt.setInt(2, roomNumber);

                        int rows = pstmt.executeUpdate();

                        if (rows > 0) {

                                System.out.println(
                                                "Price of room " + roomNumber +
                                                                " changed to $" + newPrice +
                                                                " per night.");

                        } else {

                                System.out.println("Room not found.");
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Error changing room price: " +
                                                        e.getMessage());
                }
        }

        public void removeRoom(int roomNumber) {

                String sql = "DELETE FROM rooms WHERE number = ?";

                try (Connection conn = Database.connect();
                                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setInt(1, roomNumber);

                        int rows = pstmt.executeUpdate();

                        if (rows > 0) {

                                System.out.println(
                                                "Room " + roomNumber +
                                                                " removed successfully!");

                        } else {

                                System.out.println("Room not found.");
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Error removing room: " + e.getMessage());
                }
        }

        public List<Room> getAllRooms() {

                List<Room> rooms = new ArrayList<>();

                String sql = "SELECT * FROM rooms";

                try (Connection conn = Database.connect();
                                Statement stmt = conn.createStatement();
                                ResultSet rs = stmt.executeQuery(sql)) {

                        while (rs.next()) {

                                int number = rs.getInt("number");
                                String type = rs.getString("type");
                                double price = rs.getDouble("price");
                                boolean booked = rs.getBoolean("booked");
                                boolean balcony = rs.getBoolean("balcony");

                                Room room;

                                if ("Standard".equals(type)) {

                                        room = new StandardRoom(
                                                        number,
                                                        price);

                                } else {

                                        room = new DeluxeRoom(
                                                        number,
                                                        price,
                                                        balcony);
                                }

                                room.setAvailable(!booked);

                                rooms.add(room);
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Error fetching rooms: " + e.getMessage());
                }

                return rooms;
        }

        public void showAllRooms() {

                List<Room> rooms = getAllRooms();

                if (rooms.isEmpty()) {

                        System.out.println("No rooms available.");
                        return;
                }

                for (Room room : rooms) {

                        room.displayInfo();

                        System.out.println();
                }
        }

        private Room createRoomFromResultSet(ResultSet rs) throws SQLException {

                int number = rs.getInt("number");
                String type = rs.getString("type");
                double price = rs.getDouble("price");
                boolean balcony = rs.getBoolean("balcony");

                if ("Standard".equals(type)) {

                        return new StandardRoom(
                                        number,
                                        price);

                } else {

                        return new DeluxeRoom(
                                        number,
                                        price,
                                        balcony);
                }
        }

        public void showAvailableRooms(
                        LocalDate checkIn,
                        LocalDate checkOut) {

                String sql = "SELECT r.* " +
                                "FROM rooms r " +
                                "WHERE NOT EXISTS (" +
                                "SELECT 1 FROM bookings b " +
                                "WHERE b.room_number = r.number " +
                                "AND b.check_in < ? " +
                                "AND b.check_out > ?" +
                                ")";

                boolean found = false;

                try (Connection conn = Database.connect();
                                PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, checkOut.toString());
                        stmt.setString(2, checkIn.toString());

                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {

                                Room room = createRoomFromResultSet(rs);

                                room.displayInfo();

                                System.out.println("Status: AVAILABLE");
                                System.out.println();

                                found = true;
                        }

                        if (!found) {

                                System.out.println(
                                                "No available rooms for these dates.");
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Error checking available rooms: "
                                                        + e.getMessage());
                }
        }

        public void showAllBookings() {

                String sql = "SELECT b.id, u.username, b.room_number, " +
                                "b.check_in, b.check_out, r.type, r.price " +
                                "FROM bookings b " +
                                "JOIN users u ON b.user_id = u.id " +
                                "JOIN rooms r ON b.room_number = r.number " +
                                "ORDER BY b.id";

                try (Connection conn = Database.connect();
                                PreparedStatement stmt = conn.prepareStatement(sql);
                                ResultSet rs = stmt.executeQuery()) {

                        System.out.println("\n=== ALL BOOKINGS ===");

                        boolean found = false;

                        while (rs.next()) {

                                found = true;

                                System.out.println(
                                                "Booking ID: " + rs.getInt("id"));

                                System.out.println(
                                                "Username: " + rs.getString("username"));

                                System.out.println(
                                                "Room: " + rs.getInt("room_number"));

                                System.out.println(
                                                "Check-in: " + rs.getString("check_in"));

                                System.out.println(
                                                "Check-out: " + rs.getString("check_out"));

                                System.out.println(
                                                "Type: " + rs.getString("type"));

                                System.out.println(
                                                "Price: $" + rs.getDouble("price") +
                                                                " / night");

                                System.out.println("--------------------");
                        }

                        if (!found) {

                                System.out.println(
                                                "No bookings found.");
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Error fetching bookings: " +
                                                        e.getMessage());
                }
        }

        public void bookRoom(
                        int roomNumber,
                        User user,
                        LocalDate checkIn,
                        LocalDate checkOut) {

                String checkSql = "SELECT number FROM rooms WHERE number = ?";

                String overlapSql = "SELECT COUNT(*) FROM bookings " +
                                "WHERE room_number = ? " +
                                "AND check_in < ? " +
                                "AND check_out > ?";

                String bookingSql = "INSERT INTO bookings(user_id, room_number, check_in, check_out) " +
                                "VALUES(?, ?, ?, ?)";

                try (Connection conn = Database.connect()) {

                        conn.setAutoCommit(false);

                        try {

                                // Check whether the room exists and is available
                                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                                        checkStmt.setInt(1, roomNumber);

                                        ResultSet rs = checkStmt.executeQuery();

                                        if (!rs.next()) {

                                                System.out.println("Room not found.");
                                                conn.rollback();
                                                return;
                                        }
                                }

                                // Check whether the dates overlap with an existing booking
                                try (PreparedStatement overlapStmt = conn.prepareStatement(overlapSql)) {

                                        overlapStmt.setInt(1, roomNumber);
                                        overlapStmt.setString(2, checkOut.toString());
                                        overlapStmt.setString(3, checkIn.toString());

                                        ResultSet overlapResult = overlapStmt.executeQuery();

                                        if (overlapResult.next() && overlapResult.getInt(1) > 0) {

                                                System.out.println(
                                                                "Room is already booked for these dates.");

                                                conn.rollback();
                                                return;
                                        }
                                }

                                // Create booking
                                try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {

                                        bookingStmt.setInt(
                                                        1,
                                                        getUserId(user, conn));

                                        bookingStmt.setInt(2, roomNumber);

                                        bookingStmt.setString(3, checkIn.toString());

                                        bookingStmt.setString(4, checkOut.toString());

                                        bookingStmt.executeUpdate();
                                }

                                conn.commit();

                                System.out.println(
                                                "Room " + roomNumber +
                                                                " successfully booked by " +
                                                                user.getUsername() + "!");

                        } catch (SQLException e) {

                                conn.rollback();

                                System.out.println(
                                                "Error booking room: " +
                                                                e.getMessage());
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Database error: " + e.getMessage());
                }
        }

        public boolean cancelBooking(int bookingId, User user) {

                String findBookingSql = "SELECT user_id FROM bookings WHERE id = ?";
                String deleteBookingSql = "DELETE FROM bookings WHERE id = ?";

                try (Connection conn = Database.connect()) {

                        int bookingUserId;

                        try (PreparedStatement stmt = conn.prepareStatement(findBookingSql)) {
                                stmt.setInt(1, bookingId);

                                try (ResultSet rs = stmt.executeQuery()) {
                                        if (!rs.next()) {
                                                System.out.println("Booking not found.");
                                                return false;
                                        }
                                        bookingUserId = rs.getInt("user_id");
                                }
                        }

                        boolean isAdmin = user.getRole() == Role.ADMIN;

                        if (!isAdmin && bookingUserId != getUserId(user, conn)) {
                                System.out.println("You cannot cancel this booking.");
                                return false;
                        }

                        try (PreparedStatement stmt = conn.prepareStatement(deleteBookingSql)) {
                                stmt.setInt(1, bookingId);
                                stmt.executeUpdate();
                        }

                        System.out.println("Booking " + bookingId + " has been cancelled.");
                        return true;

                } catch (SQLException e) {
                        System.out.println("Error cancelling booking: " + e.getMessage());
                        return false;
                }
        }

        public void showMyBookings(User user) {

                String sql = "SELECT b.id, b.check_in, b.check_out, " +
                                "r.number, r.type, r.price, r.balcony " +
                                "FROM bookings b " +
                                "JOIN rooms r ON b.room_number = r.number " +
                                "WHERE b.user_id = ?";

                try (Connection conn = Database.connect();
                                PreparedStatement stmt = conn.prepareStatement(sql)) {

                        int userId = getUserId(user, conn);

                        stmt.setInt(1, userId);

                        ResultSet rs = stmt.executeQuery();

                        boolean found = false;

                        System.out.println("\n=== MY BOOKINGS ===");

                        while (rs.next()) {

                                found = true;

                                System.out.println(
                                                "Booking ID: " + rs.getInt("id"));

                                System.out.println(
                                                "Room: " + rs.getInt("number"));

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
                                                "Status: BOOKED BY YOU");

                                System.out.println();
                        }

                        if (!found) {

                                System.out.println(
                                                "You have no bookings.");
                        }

                } catch (SQLException e) {

                        System.out.println(
                                        "Error fetching your bookings: " +
                                                        e.getMessage());
                }
        }

        private int getUserId(
                        User user,
                        Connection conn) throws SQLException {

                String sql = "SELECT id FROM users WHERE username = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(
                                        1,
                                        user.getUsername());

                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {

                                return rs.getInt("id");
                        }
                }

                throw new SQLException(
                                "User not found in database.");
        }
}