package com.hotelbooking.hotel_booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Room;
import com.hotelbooking.hotel_booking.model.RoomType;

@SpringBootTest
@Transactional
class RoomServiceTest {

    @Autowired
    RoomService roomService;
    @Autowired
    UserService userService;
    @Autowired
    BookingService bookingService;

    private final LocalDate base = LocalDate.now().plusDays(30);

    @BeforeEach
    void setUp() {
        roomService.addRoom(101, RoomType.STANDARD, new BigDecimal("50.00"), false);
    }

    @Test
    void addedRoomAppearsInList() {
        roomService.addRoom(102, RoomType.DELUXE, new BigDecimal("120.00"), true);

        assertEquals(2, roomService.findAll().size());
    }

    @Test
    void duplicateRoomNumberIsRejected() {
        assertThrows(ServiceException.class,
                () -> roomService.addRoom(101, RoomType.DELUXE, new BigDecimal("99.00"), true));
    }

    @Test
    void nonPositivePriceIsRejected() {
        assertThrows(ServiceException.class, () -> roomService.addRoom(103, RoomType.STANDARD, BigDecimal.ZERO, false));
    }

    @Test
    void changePriceUpdatesRoom() {
        roomService.changePrice(101, new BigDecimal("75.00"));

        Room room = roomService.findAll().get(0);
        assertEquals(0, new BigDecimal("75.00").compareTo(room.getPricePerNight()));
    }

    @Test
    void roomWithoutBookingsCanBeRemoved() {
        roomService.removeRoom(101);

        assertTrue(roomService.findAll().isEmpty());
    }

    @Test
    void roomWithBookingCannotBeRemoved() {
        userService.register("alice", "secret123");
        bookingService.book("alice", 101, base, base.plusDays(3));

        assertThrows(ServiceException.class, () -> roomService.removeRoom(101));
        assertEquals(1, roomService.findAll().size());
    }

    @Test
    void availableRoomsExcludeBookedOnes() {
        roomService.addRoom(102, RoomType.DELUXE, new BigDecimal("120.00"), true);
        userService.register("alice", "secret123");
        bookingService.book("alice", 101, base, base.plusDays(5));

        List<Room> free = roomService.findAvailable(base.plusDays(1), base.plusDays(3));

        assertEquals(1, free.size());
        assertEquals(102, free.get(0).getRoomNumber());
    }
}