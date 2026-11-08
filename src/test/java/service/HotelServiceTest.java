package service;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.DeluxeRoom;
import model.Room;
import model.StandardRoom;
import model.User;

class HotelServiceTest {

        private HotelService hotel;
        private UserService users;
        private User alice;
        private User admin;
        private User bob;

        @BeforeEach
        void setUp(@TempDir Path dir) {
                Database.setUrl("jdbc:sqlite:" + dir.resolve("test.db"));
                Database.initialize();

                hotel = new HotelService();
                users = new UserService();

                users.register("alice", "pw1");
                users.register("bob", "pw2");
                alice = users.login("alice", "pw1");
                bob = users.login("bob", "pw2");
                admin = users.login("admin", "admin123");

                hotel.addRoom(new StandardRoom(101, 50.0));
        }

        private int countBookings() throws Exception {
                try (Connection c = Database.connect();
                                ResultSet rs = c.createStatement()
                                                .executeQuery("SELECT COUNT(*) FROM bookings")) {
                        rs.next();
                        return rs.getInt(1);
                }
        }

        private int firstBookingId(String username) throws Exception {
                try (Connection c = Database.connect();
                                ResultSet rs = c.createStatement().executeQuery(
                                                "SELECT b.id FROM bookings b JOIN users u ON u.id = b.user_id " +
                                                                "WHERE u.username = '" + username
                                                                + "' ORDER BY b.id LIMIT 1")) {
                        rs.next();
                        return rs.getInt(1);
                }
        }

        @Test
        void overlappingBookingIsRejected() throws Exception {
                assertTrue(hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15)));

                assertFalse(hotel.bookRoom(101, bob,
                                LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 18)));

                assertEquals(1, countBookings());
        }

        @Test
        void checkoutDayCanBeNextCheckinDay() throws Exception {
                assertTrue(hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15)));

                assertTrue(hotel.bookRoom(101, bob,
                                LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 20)));

                assertEquals(2, countBookings());
        }

        @Test
        void cancelDeletesOnlyThatBooking() throws Exception {
                hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));
                hotel.bookRoom(101, bob,
                                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 22));

                assertTrue(hotel.cancelBooking(firstBookingId("alice"), alice));

                assertEquals(1, countBookings());
        }

        @Test
        void clientCannotCancelSomeoneElsesBooking() throws Exception {
                hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));

                assertFalse(hotel.cancelBooking(firstBookingId("alice"), bob));

                assertEquals(1, countBookings());
        }

        @Test
        void checkoutBeforeCheckinIsRejected() throws Exception {
                assertFalse(hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 10)));

                assertEquals(0, countBookings());
        }

        @Test
        void addedRoomAppearsInList() {
                hotel.addRoom(new DeluxeRoom(102, 120.0, true));
                assertEquals(2, hotel.getAllRooms().size());
        }

        @Test
        void duplicateRoomNumberIsNotAdded() {
                hotel.addRoom(new StandardRoom(101, 99.0));
                List<Room> rooms = hotel.getAllRooms();
                assertEquals(1, rooms.size());
                assertEquals(50.0, rooms.get(0).getPricePerNight(), 0.001);
        }

        @Test
        void changeRoomPriceUpdatesPrice() {
                hotel.changeRoomPrice(101, 75.0);
                assertEquals(75.0, hotel.getAllRooms().get(0).getPricePerNight(), 0.001);
        }

        @Test
        void roomWithoutBookingsCanBeRemoved() {
                hotel.removeRoom(101);
                assertTrue(hotel.getAllRooms().isEmpty());
        }

        @Test
        void roomWithBookingCannotBeRemoved() throws Exception {
                hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));

                hotel.removeRoom(101);

                assertEquals(1, hotel.getAllRooms().size());
                assertEquals(1, countBookings());
        }

        @Test
        void bookingUnknownRoomIsRejected() {
                assertFalse(hotel.bookRoom(999, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)));
        }

        @Test
        void cancellingUnknownBookingReturnsFalse() {
                assertFalse(hotel.cancelBooking(9999, alice));
        }

        @Test
        void adminCanCancelAnyBooking() throws Exception {
                hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));

                assertTrue(hotel.cancelBooking(firstBookingId("alice"), admin));
                assertEquals(0, countBookings());
        }

        @Test
        void bookingInsideExistingBookingIsRejected() throws Exception {
                hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 20));

                assertFalse(hotel.bookRoom(101, bob,
                                LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 14)));
        }

        @Test
        void bookingCoveringExistingBookingIsRejected() throws Exception {
                hotel.bookRoom(101, alice,
                                LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 14));

                assertFalse(hotel.bookRoom(101, bob,
                                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 20)));
        }
}