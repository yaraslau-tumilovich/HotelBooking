package com.hotelbooking.hotel_booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Role;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.model.User;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    RoomService roomService;
    @Autowired
    BookingService bookingService;
    @Autowired
    UserRepository users;
    @Autowired
    BookingRepository bookings;
    @Autowired
    PasswordEncoder encoder;

    private final LocalDate base = LocalDate.now().plusDays(30);

    private LocalDate d(int days) {
        return base.plusDays(days);
    }

    @BeforeEach
    void setUp() {
        userService.register("alice", "secret123");
        userService.register("bob", "secret456");
        users.save(new User("boss", encoder.encode("secret789"), Role.ADMIN));
        roomService.addRoom(101, RoomType.STANDARD, new BigDecimal("50.00"), false);
    }

    @Test
    void bookingIsSaved() {
        Booking booking = bookingService.book("alice", 101, d(0), d(5));

        assertNotNull(booking.getId());
        assertEquals(1, bookings.count());
    }

    @Test
    void overlappingBookingIsRejected() {
        bookingService.book("alice", 101, d(0), d(5));

        assertThrows(ServiceException.class,
                () -> bookingService.book("bob", 101, d(2), d(8)));
        assertEquals(1, bookings.count());
    }

    @Test
    void checkoutDayCanBeNextCheckinDay() {
        bookingService.book("alice", 101, d(0), d(5));

        bookingService.book("bob", 101, d(5), d(9));

        assertEquals(2, bookings.count());
    }

    @Test
    void bookingInsideExistingBookingIsRejected() {
        bookingService.book("alice", 101, d(0), d(10));

        assertThrows(ServiceException.class,
                () -> bookingService.book("bob", 101, d(2), d(4)));
    }

    @Test
    void bookingCoveringExistingBookingIsRejected() {
        bookingService.book("alice", 101, d(2), d(4));

        assertThrows(ServiceException.class,
                () -> bookingService.book("bob", 101, d(0), d(10)));
    }

    @Test
    void checkoutBeforeCheckinIsRejected() {
        assertThrows(ServiceException.class,
                () -> bookingService.book("alice", 101, d(5), d(0)));
    }

    @Test
    void pastCheckinIsRejected() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThrows(ServiceException.class,
                () -> bookingService.book("alice", 101, yesterday, d(0)));
    }

    @Test
    void unknownRoomIsRejected() {
        assertThrows(ServiceException.class,
                () -> bookingService.book("alice", 999, d(0), d(2)));
    }

    @Test
    void unknownUserIsRejected() {
        assertThrows(ServiceException.class,
                () -> bookingService.book("nobody", 101, d(0), d(2)));
    }

    @Test
    void cancelRemovesOnlyThatBooking() {
        Booking first = bookingService.book("alice", 101, d(0), d(2));
        bookingService.book("bob", 101, d(10), d(12));

        bookingService.cancel(first.getId(), "alice");

        assertEquals(1, bookings.count());
        assertEquals("bob", bookings.findAll().get(0).getUser().getUsername());
    }

    @Test
    void clientCannotCancelSomeoneElsesBooking() {
        Booking booking = bookingService.book("alice", 101, d(0), d(2));

        assertThrows(ServiceException.class,
                () -> bookingService.cancel(booking.getId(), "bob"));
        assertEquals(1, bookings.count());
    }

    @Test
    void adminCanCancelAnyBooking() {
        Booking booking = bookingService.book("alice", 101, d(0), d(2));

        bookingService.cancel(booking.getId(), "boss");

        assertEquals(0, bookings.count());
    }

    @Test
    void cancellingUnknownBookingIsRejected() {
        assertThrows(ServiceException.class,
                () -> bookingService.cancel(9999L, "alice"));
    }
}