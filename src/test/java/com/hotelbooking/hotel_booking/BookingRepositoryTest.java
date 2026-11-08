package com.hotelbooking.hotel_booking;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.model.*;
import com.hotelbooking.hotel_booking.repository.*;

@SpringBootTest
@Transactional
class BookingRepositoryTest {

    @Autowired
    UserRepository users;
    @Autowired
    RoomRepository rooms;
    @Autowired
    BookingRepository bookings;

    private Room room101;

    @BeforeEach
    void setUp() {
        User alice = users.save(new User("alice", "hash", Role.USER));
        room101 = rooms.save(new Room(101, RoomType.STANDARD, new BigDecimal("50.00"), false));
        rooms.save(new Room(102, RoomType.DELUXE, new BigDecimal("120.00"), true));
        bookings.save(new Booking(alice, room101,
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15)));
    }

    @Test
    void overlappingDatesAreDetected() {
        assertEquals(1, bookings.countOverlapping(room101,
                LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 18)));
    }

    @Test
    void checkoutDayIsNotAnOverlap() {
        assertEquals(0, bookings.countOverlapping(room101,
                LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 20)));
    }

    @Test
    void availableRoomsExcludeBookedOnes() {
        List<Room> free = rooms.findAvailable(
                LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 14));
        assertEquals(1, free.size());
        assertEquals(102, free.get(0).getRoomNumber());
    }

    @Test
    void userCanBeFoundByUsername() {
        assertTrue(users.findByUsername("alice").isPresent());
        assertFalse(users.existsByUsername("bob"));
    }
}