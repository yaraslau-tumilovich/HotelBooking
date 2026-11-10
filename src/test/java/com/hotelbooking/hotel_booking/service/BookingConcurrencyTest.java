package com.hotelbooking.hotel_booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hotelbooking.hotel_booking.exception.ConflictException;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;

@SpringBootTest
class BookingConcurrencyTest {

    private static final int THREADS = 8;
    private static final int ROOM = 9001;

    @Autowired
    BookingService bookingService;
    @Autowired
    RoomService roomService;
    @Autowired
    UserService userService;
    @Autowired
    BookingRepository bookings;
    @Autowired
    UserRepository users;

    @BeforeEach
    void setUp() {
        roomService.addRoom(ROOM, RoomType.STANDARD, new BigDecimal("50.00"), false);
        for (int i = 0; i < THREADS; i++) {
            userService.register("racer" + i, "password" + i);
        }
    }

    @AfterEach
    void cleanUp() {
        bookings.deleteAll();
        roomService.removeRoom(ROOM);
        for (int i = 0; i < THREADS; i++) {
            users.findByUsername("racer" + i).ifPresent(users::delete);
        }
    }

    @Test
    void onlyOneOfManyConcurrentRequestsGetsTheRoom() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(60);
        LocalDate checkOut = checkIn.plusDays(2);

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            String username = "racer" + i;
            results.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    bookingService.book(username, ROOM, checkIn, checkOut);
                    return true;
                } catch (ConflictException e) {
                    return false;
                }
            }));
        }

        ready.await();
        start.countDown();

        int succeeded = 0;
        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) {
                succeeded++;
            }
        }
        pool.shutdown();

        assertEquals(1, succeeded, "exactly one request must win the room");
        assertEquals(1, bookings.count(), "exactly one booking must be stored");
    }
}