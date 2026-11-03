package model;

public class Room {
    private int roomNumber;
    private double pricePerNight;

    public Room(int roomNumber, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void displayInfo() {
        System.out.println("Room " + roomNumber + ": $" + pricePerNight + " per night");
    }
}
