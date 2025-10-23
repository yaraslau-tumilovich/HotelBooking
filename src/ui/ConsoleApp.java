package ui;

import service.HotelService;
import model.StandardRoom;
import model.DeluxeRoom;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ConsoleApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        HotelService hotelService = new HotelService();

        System.out.println("=== Welcome to Hotel Booking App ===");

        boolean running = true;
        while (running) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Add room");
            System.out.println("2. Show all rooms");
            System.out.println("3. Show available rooms");
            System.out.println("4. Book room");
            System.out.println("5. Release room");
            System.out.println("6. Remove room");
            System.out.println("0. Exit");

            int choice = readInt(scanner, "\nEnter your choice: ");

            switch (choice) {
                case 1:
                    int number = readInt(scanner, "Enter room number: ");
                    int type = readInt(scanner, "Enter room type (1 - Standard, 2 - Deluxe): ");

                    if (type == 1) {
                        hotelService.addRoom(new StandardRoom(number, 100));
                    } else if (type == 2) {
                        boolean balcony = readBoolean(scanner, "Does it have balcony? (true/false): ");
                        hotelService.addRoom(new DeluxeRoom(number, 200, balcony));
                    } else {
                        System.out.println("\nInvalid room type.");
                    }
                    break;

                case 2:
                    hotelService.showAllRooms();
                    break;

                case 3:
                    hotelService.showAvailableRooms();
                    break;

                case 4:
                    int bookNumber = readInt(scanner, "Enter room number to book: ");
                    hotelService.bookRoom(bookNumber);
                    break;

                case 5:
                    int releaseNumber = readInt(scanner, "Enter room number to release: ");
                    hotelService.releaseRoom(releaseNumber);
                    break;

                case 6:
                    int removeNumber = readInt(scanner, "Enter room number to remove: ");
                    hotelService.removeRoom(removeNumber);
                    break;

                case 0:
                    running = false;
                    System.out.println("\nExiting app. Goodbye!");
                    break;

                default:
                    System.out.println("\nInvalid choice, try again.");
            }
        }

        scanner.close();
    }

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            try {
                int value = scanner.nextInt();
                scanner.nextLine(); 
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Error: you need to enter a number!");
                scanner.nextLine(); 
            }
        }
    }

    private static boolean readBoolean(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            try {
                boolean value = scanner.nextBoolean();
                scanner.nextLine(); 
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Error: you need to enter true or false!");
                scanner.nextLine(); 
            }
        }
    }
}
