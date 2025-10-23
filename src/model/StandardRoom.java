package model;

public class StandardRoom extends Room {

    public StandardRoom(int roomNumber, double pricePerNight) {
        super(roomNumber, pricePerNight); 
    }

    @Override
    public void displayInfo() {
        System.out.println("Standard Room #" + getRoomNumber() + " — $" + getPricePerNight());
    }
}
