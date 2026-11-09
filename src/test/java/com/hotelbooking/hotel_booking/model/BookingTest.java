package com.hotelbooking.hotel_booking.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class BookingTest {

    private final User user = new User("alice", "hash", Role.USER);
    private final Room room = new Room(101, RoomType.STANDARD, new BigDecimal("50.00"), false);

    @Test
    void totalPriceIsNightsTimesNightlyPrice() {
        Booking booking = new Booking(user, room,
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13));

        assertEquals(3, booking.getNights());
        assertEquals(0, new BigDecimal("150.00").compareTo(booking.getTotalPrice()));
    }

    @Test
    void checkoutMustBeAfterCheckin() {
        assertThrows(IllegalArgumentException.class, () -> new Booking(user, room,
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 10)));
    }
}