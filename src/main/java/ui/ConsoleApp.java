package ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

import model.DeluxeRoom;
import model.Role;
import model.StandardRoom;
import model.User;
import service.HotelService;
import service.UserService;

public class ConsoleApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        HotelService hotelService = new HotelService();
        UserService userService = new UserService();

        boolean running = true;

        while (running) {

            System.out.println("\n=== HOTEL BOOKING APP ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");

            int choice = readInt(
                    scanner,
                    "\nEnter your choice: ");

            switch (choice) {

                case 1:

                    User user = login(
                            scanner,
                            userService);

                    if (user != null) {

                        System.out.println(
                                "\nWelcome, " +
                                        user.getUsername() +
                                        "!");

                        System.out.println(
                                "Your role: " +
                                        user.getRole());

                        boolean loggedIn = true;

                        while (loggedIn) {

                            if (user.getRole() == Role.ADMIN) {

                                loggedIn = showAdminMenu(
                                        scanner,
                                        hotelService,
                                        userService,
                                        user);

                            } else {

                                loggedIn = showClientMenu(
                                        scanner,
                                        hotelService,
                                        userService,
                                        user);
                            }
                        }
                    }

                    break;

                case 2:

                    register(
                            scanner,
                            userService);

                    break;

                case 0:

                    running = false;

                    System.out.println(
                            "\nGoodbye!");

                    break;

                default:

                    System.out.println(
                            "\nInvalid choice.");
            }
        }

        scanner.close();
    }

    private static User login(
            Scanner scanner,
            UserService userService) {

        System.out.println("\n=== LOGIN ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userService.login(
                username,
                password);

        if (user != null) {

            System.out.println(
                    "\nLogin successful!");

            return user;
        }

        System.out.println(
                "\nInvalid username or password.");

        return null;
    }

    private static void register(
            Scanner scanner,
            UserService userService) {

        System.out.println("\n=== REGISTER ===");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        boolean success = userService.register(
                username,
                password);

        if (success) {

            System.out.println(
                    "\nAccount created successfully!");

            System.out.println(
                    "You can now login.");

        } else {

            System.out.println(
                    "\nRegistration failed.");

            System.out.println(
                    "Username may already exist.");
        }
    }

    private static boolean showAdminMenu(
            Scanner scanner,
            HotelService hotelService,
            UserService userService,
            User user) {

        System.out.println("\n=== ADMIN MENU ===");

        System.out.println("1. Add room");
        System.out.println("2. Show all rooms");
        System.out.println("3. Show available rooms");
        System.out.println("4. Book room");
        System.out.println("5. Cancel booking");
        System.out.println("6. Remove room");
        System.out.println("7. Change room price");
        System.out.println("8. User management");
        System.out.println("0. Logout");

        int choice = readInt(
                scanner,
                "\nEnter your choice: ");

        switch (choice) {

            case 1:

                int number = readInt(
                        scanner,
                        "Enter room number: ");

                int type = readInt(
                        scanner,
                        "Enter room type " +
                                "(1 - Standard, 2 - Deluxe): ");

                System.out.print("Enter price per night ($): ");
                double price = scanner.nextDouble();
                scanner.nextLine();

                if (price <= 0) {

                    System.out.println(
                            "\nPrice must be greater than 0.");

                } else if (type == 1) {

                    hotelService.addRoom(
                            new StandardRoom(
                                    number,
                                    price));

                } else if (type == 2) {

                    boolean balcony = readBoolean(
                            scanner,
                            "Does it have balcony? " +
                                    "(true/false): ");

                    hotelService.addRoom(
                            new DeluxeRoom(
                                    number,
                                    price,
                                    balcony));

                } else {

                    System.out.println(
                            "\nInvalid room type.");
                }

                break;

            case 2:

                hotelService.showAllRooms();

                break;

            case 3:

                LocalDate checkIn = readDate(
                        scanner,
                        "Enter check-in date (YYYY-MM-DD): ");

                LocalDate checkOut = readCheckOutDate(
                        scanner,
                        checkIn);

                hotelService.showAvailableRooms(
                        checkIn,
                        checkOut);

                break;

            case 4:

                int bookNumber = readInt(
                        scanner,
                        "Enter room number to book: ");

                LocalDate bookingCheckIn = readDate(
                        scanner,
                        "Enter check-in date (YYYY-MM-DD): ");

                LocalDate bookingCheckOut = readCheckOutDate(
                        scanner,
                        bookingCheckIn);

                hotelService.bookRoom(
                        bookNumber,
                        user,
                        bookingCheckIn,
                        bookingCheckOut);

                break;

            case 5:

                int bookingId = readInt(
                        scanner,
                        "Enter Booking ID to cancel: ");

                hotelService.cancelBooking(
                        bookingId,
                        user);

                break;

            case 6:

                int removeNumber = readInt(
                        scanner,
                        "Enter room number to remove: ");

                hotelService.removeRoom(
                        removeNumber);

                break;

            case 7:

                int priceRoomNumber = readInt(
                        scanner,
                        "Enter room number: ");

                System.out.print(
                        "Enter new price per night ($): ");

                double newPrice = scanner.nextDouble();
                scanner.nextLine();

                hotelService.changeRoomPrice(
                        priceRoomNumber,
                        newPrice);

                break;

            case 8:

                boolean userManagement = true;

                while (userManagement) {

                    System.out.println("\n=== USER MANAGEMENT ===");

                    System.out.println("1. View all accounts");
                    System.out.println("2. View user's bookings");
                    System.out.println("3. Delete account");
                    System.out.println("0. Back");

                    int userChoice = readInt(
                            scanner,
                            "\nEnter your choice: ");

                    switch (userChoice) {

                        case 1:

                            userService.showAllUsers();

                            break;

                        case 2:

                            System.out.print(
                                    "Enter username: ");

                            String bookingUsername = scanner.nextLine();

                            userService.showUserBookings(
                                    bookingUsername);

                            break;

                        case 3:

                            System.out.print(
                                    "Enter username to delete: ");

                            String deleteUsername = scanner.nextLine();

                            System.out.print(
                                    "Type YES to confirm deletion: ");

                            String confirmation = scanner.nextLine();

                            if (confirmation.equals("YES")) {

                                userService.deleteUserByUsername(
                                        deleteUsername);

                            } else {

                                System.out.println(
                                        "Deletion cancelled.");
                            }

                            break;

                        case 0:

                            userManagement = false;

                            break;

                        default:

                            System.out.println(
                                    "Invalid choice.");
                    }
                }

                break;

            case 0:

                System.out.println(
                        "\nLogging out...");

                return false;

            default:

                System.out.println(
                        "\nInvalid choice.");
        }

        return true;
    }

    private static boolean showClientMenu(
            Scanner scanner,
            HotelService hotelService,
            UserService userService,
            User user) {

        System.out.println("\n=== CLIENT MENU ===");

        System.out.println("1. Show all rooms");
        System.out.println("2. Show available rooms");
        System.out.println("3. Book room");
        System.out.println("4. Cancel my booking");
        System.out.println("5. My bookings");
        System.out.println("6. Delete my account");
        System.out.println("0. Logout");

        int choice = readInt(
                scanner,
                "\nEnter your choice: ");

        switch (choice) {

            case 1:

                hotelService.showAllRooms();

                break;

            case 2:

                LocalDate checkIn = readDate(
                        scanner,
                        "Enter check-in date (YYYY-MM-DD): ");

                LocalDate checkOut = readCheckOutDate(
                        scanner,
                        checkIn);

                hotelService.showAvailableRooms(
                        checkIn,
                        checkOut);

                break;

            case 3:

                int bookNumber = readInt(
                        scanner,
                        "Enter room number to book: ");

                LocalDate bookingCheckIn = readDate(
                        scanner,
                        "Enter check-in date (YYYY-MM-DD): ");

                LocalDate bookingCheckOut = readCheckOutDate(
                        scanner,
                        bookingCheckIn);

                hotelService.bookRoom(
                        bookNumber,
                        user,
                        bookingCheckIn,
                        bookingCheckOut);

                break;

            case 4:

                int bookingId = readInt(
                        scanner,
                        "Enter Booking ID to cancel: ");

                hotelService.cancelBooking(
                        bookingId,
                        user);

                break;

            case 5:

                hotelService.showMyBookings(
                        user);

                break;

            case 6:

                System.out.println(
                        "\nAre you sure you want to delete your account?");

                System.out.print(
                        "Type YES to confirm: ");

                String confirmation = scanner.nextLine();

                if (confirmation.equals("YES")) {

                    boolean deleted = userService.deleteAccount(user);

                    if (deleted) {
                        return false;
                    }

                } else {

                    System.out.println(
                            "Account deletion cancelled.");
                }

                break;

            case 0:

                System.out.println(
                        "\nLogging out...");

                return false;

            default:

                System.out.println(
                        "\nInvalid choice.");
        }

        return true;
    }

    private static int readInt(
            Scanner scanner,
            String message) {

        while (true) {

            System.out.print(message);

            try {

                int value = scanner.nextInt();

                scanner.nextLine();

                return value;

            } catch (InputMismatchException e) {

                System.out.println(
                        "Error: you need to enter a number!");

                scanner.nextLine();
            }
        }
    }

    private static LocalDate readDate(Scanner scanner, String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine();

            try {
                return LocalDate.parse(input);

            } catch (DateTimeParseException e) {

                System.out.println(
                        "Invalid date. Use format YYYY-MM-DD.");
            }
        }
    }

    private static LocalDate readCheckOutDate(
            Scanner scanner,
            LocalDate checkIn) {

        while (true) {

            LocalDate checkOut = readDate(
                    scanner,
                    "Enter check-out date (YYYY-MM-DD): ");

            if (checkOut.isAfter(checkIn)) {
                return checkOut;
            }

            System.out.println(
                    "Check-out date must be after check-in date.");
        }
    }

    private static boolean readBoolean(
            Scanner scanner,
            String message) {

        while (true) {

            System.out.print(message);

            try {

                boolean value = scanner.nextBoolean();

                scanner.nextLine();

                return value;

            } catch (InputMismatchException e) {

                System.out.println(
                        "Error: you need to enter true or false!");

                scanner.nextLine();
            }
        }
    }
}