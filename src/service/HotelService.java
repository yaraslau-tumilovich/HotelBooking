package service;

import model.Room;
import model.StandardRoom;
import model.DeluxeRoom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelService {

    public HotelService() {
        Database.initialize();
    }

    public void addRoom(Room room) {
        String sql = "INSERT INTO rooms(number,type,price,booked,balcony) VALUES(?,?,?,?,?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room instanceof DeluxeRoom ? "Deluxe" : "Standard");
            pstmt.setDouble(3, room.getPricePerNight());
            pstmt.setBoolean(4, room.isAvailable() ? false : true); // false = свободна
            pstmt.setBoolean(5, room instanceof DeluxeRoom ? ((DeluxeRoom) room).hasBalcony() : false);

            pstmt.executeUpdate();
            System.out.println("Room " + room.getRoomNumber() + " added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding room: " + e.getMessage());
        }
    }

    public void removeRoom(int roomNumber) {
        String sql = "DELETE FROM rooms WHERE number = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Room " + roomNumber + " removed successfully!");
            } else {
                System.out.println("Room not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error removing room: " + e.getMessage());
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
                    room = new StandardRoom(number, price);
                } else {
                    room = new DeluxeRoom(number, price, balcony);
                }
                room.setAvailable(!booked);
                rooms.add(room);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching rooms: " + e.getMessage());
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
        }
    }

    public void showAvailableRooms() {
        List<Room> rooms = getAllRooms();
        boolean found = false;
        for (Room room : rooms) {
            if (room.isAvailable()) {
                room.displayInfo();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No available rooms at the moment.");
        }
    }

    public void bookRoom(int roomNumber) {
        updateBookingStatus(roomNumber, true);
    }

    public void releaseRoom(int roomNumber) {
        updateBookingStatus(roomNumber, false);
    }

    private void updateBookingStatus(int roomNumber, boolean booked) {
        String sql = "UPDATE rooms SET booked = ? WHERE number = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, booked);
            pstmt.setInt(2, roomNumber);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Room " + roomNumber + (booked ? " successfully booked!" : " is now available again!"));
            } else {
                System.out.println("Room not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating room status: " + e.getMessage());
        }
    }
}
