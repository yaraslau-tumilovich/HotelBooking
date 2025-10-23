package model;

public class DeluxeRoom extends Room {
    private boolean hasBalcony;

    public DeluxeRoom(int roomNumber, double pricePerNight, boolean hasBalcony) {
        super(roomNumber, pricePerNight);
        this.hasBalcony = hasBalcony;
    }

    public boolean hasBalcony() {
        return hasBalcony;
    }

    @Override
    public void displayInfo() {
        System.out.println("Deluxe Room #" + getRoomNumber() +
                " — $" + getPricePerNight() +
                (hasBalcony ? " with balcony" : ""));
    }
}
