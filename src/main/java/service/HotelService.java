package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.DeluxeRoom;
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
                            : "Standard"
            );

            pstmt.setDouble(3, room.getPricePerNight());

            pstmt.setBoolean(4, false);

            pstmt.setBoolean(
                    5,
                    room instanceof DeluxeRoom
                            && ((DeluxeRoom) room).hasBalcony()
            );

            pstmt.executeUpdate();

            System.out.println(
                    "Room " + room.getRoomNumber() +
                    " added successfully!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Error adding room: " + e.getMessage()
            );
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
                        " per night."
                );

            } else {

                System.out.println("Room not found.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error changing room price: " +
                    e.getMessage()
            );
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
                        " removed successfully!"
                );

            } else {

                System.out.println("Room not found.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error removing room: " + e.getMessage()
            );
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
                            price
                    );

                } else {

                    room = new DeluxeRoom(
                            number,
                            price,
                            balcony
                    );
                }

                room.setAvailable(!booked);

                rooms.add(room);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error fetching rooms: " + e.getMessage()
            );
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

            if (!room.isAvailable()) {

                System.out.println("Status: BOOKED");
            } else {

                System.out.println("Status: AVAILABLE");
            }

            System.out.println();
        }
    }

    public void showAvailableRooms() {

        List<Room> rooms = getAllRooms();

        boolean found = false;

        for (Room room : rooms) {

            if (room.isAvailable()) {

                room.displayInfo();

                System.out.println("Status: AVAILABLE");
                System.out.println();

                found = true;
            }
        }

        if (!found) {

            System.out.println(
                    "No available rooms at the moment."
            );
        }
    }

    public void bookRoom(int roomNumber, User user) {

        String checkSql =
                "SELECT booked FROM rooms WHERE number = ?";

        String bookingSql =
                "INSERT INTO bookings(user_id, room_number) " +
                "VALUES(?, ?)";

        String updateRoomSql =
                "UPDATE rooms SET booked = true WHERE number = ?";

        try (Connection conn = Database.connect()) {

            conn.setAutoCommit(false);

            try {

                // Check whether the room exists and is available
                try (PreparedStatement checkStmt =
                             conn.prepareStatement(checkSql)) {

                    checkStmt.setInt(1, roomNumber);

                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next()) {

                        System.out.println("Room not found.");
                        conn.rollback();
                        return;
                    }

                    boolean booked = rs.getBoolean("booked");

                    if (booked) {

                        System.out.println(
                                "Room is already booked."
                        );

                        conn.rollback();
                        return;
                    }
                }

                // Create booking
                try (PreparedStatement bookingStmt =
                             conn.prepareStatement(bookingSql)) {

                    bookingStmt.setInt(
                            1,
                            getUserId(user, conn)
                    );

                    bookingStmt.setInt(2, roomNumber);

                    bookingStmt.executeUpdate();
                }

                // Mark room as booked
                try (PreparedStatement updateStmt =
                             conn.prepareStatement(updateRoomSql)) {

                    updateStmt.setInt(1, roomNumber);

                    updateStmt.executeUpdate();
                }

                conn.commit();

                System.out.println(
                        "Room " + roomNumber +
                        " successfully booked by " +
                        user.getUsername() + "!"
                );

            } catch (SQLException e) {

                conn.rollback();

                System.out.println(
                        "Error booking room: " +
                        e.getMessage()
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database error: " + e.getMessage()
            );
        }
    }

    public void releaseRoom(int roomNumber, User user) {

        String findBookingSql =
                "SELECT b.user_id " +
                "FROM bookings b " +
                "WHERE b.room_number = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt =
                     conn.prepareStatement(findBookingSql)) {

            stmt.setInt(1, roomNumber);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {

                System.out.println(
                        "This room is not booked."
                );

                return;
            }

            int bookingUserId = rs.getInt("user_id");

            int currentUserId = getUserId(user, conn);

            boolean isAdmin =
                    user.getRole().name().equals("ADMIN");

            if (!isAdmin && bookingUserId != currentUserId) {

                System.out.println(
                        "You cannot release this room."
                );

                return;
            }

            deleteBookingAndFreeRoom(
                    roomNumber,
                    conn
            );

            System.out.println(
                    "Room " + roomNumber +
                    " is now available again!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Error releasing room: " +
                    e.getMessage()
            );
        }
    }

    public void showMyBookings(User user) {

        String sql =
                "SELECT r.number, r.type, r.price, r.balcony " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_number = r.number " +
                "WHERE b.user_id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            int userId = getUserId(user, conn);

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            boolean found = false;

            System.out.println("\n=== MY BOOKINGS ===");

            while (rs.next()) {

                found = true;

                System.out.println(
                        "Room: " + rs.getInt("number")
                );

                System.out.println(
                        "Type: " + rs.getString("type")
                );

                System.out.println(
                        "Price: $" + rs.getDouble("price") +
                        " / night"
                );

                if ("Deluxe".equals(rs.getString("type"))) {

                    System.out.println(
                            "Balcony: " +
                            (rs.getBoolean("balcony")
                                    ? "Yes"
                                    : "No")
                    );
                }

                System.out.println(
                        "Status: BOOKED BY YOU"
                );

                System.out.println();
            }

            if (!found) {

                System.out.println(
                        "You have no bookings."
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error fetching your bookings: " +
                    e.getMessage()
            );
        }
    }

    private int getUserId(
            User user,
            Connection conn) throws SQLException {

        String sql =
                "SELECT id FROM users WHERE username = ?";

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(
                    1,
                    user.getUsername()
            );

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return rs.getInt("id");
            }
        }

        throw new SQLException(
                "User not found in database."
        );
    }

    private void deleteBookingAndFreeRoom(
            int roomNumber,
            Connection conn) throws SQLException {

        String deleteBookingSql =
                "DELETE FROM bookings WHERE room_number = ?";

        String freeRoomSql =
                "UPDATE rooms SET booked = false " +
                "WHERE number = ?";

        try (PreparedStatement bookingStmt =
                     conn.prepareStatement(deleteBookingSql);
             PreparedStatement roomStmt =
                     conn.prepareStatement(freeRoomSql)) {

            bookingStmt.setInt(1, roomNumber);
            bookingStmt.executeUpdate();

            roomStmt.setInt(1, roomNumber);
            roomStmt.executeUpdate();
        }
    }
}