package model;

import java.time.LocalDate;

public class Booking {

    private int id;
    private int userId;
    private int roomNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Booking(int id, int userId, int roomNumber,
                   LocalDate checkIn, LocalDate checkOut) {
        this.id = id;
        this.userId = userId;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
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

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }
}