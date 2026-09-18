package model;

public class Booking {

    private int id;
    private int userId;
    private int roomNumber;

    public Booking(int id, int userId, int roomNumber) {
        this.id = id;
        this.userId = userId;
        this.roomNumber = roomNumber;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }
}