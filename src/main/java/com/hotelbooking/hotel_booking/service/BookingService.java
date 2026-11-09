package com.hotelbooking.hotel_booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.exception.ConflictException;
import com.hotelbooking.hotel_booking.exception.ForbiddenException;
import com.hotelbooking.hotel_booking.exception.NotFoundException;
import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Role;
import com.hotelbooking.hotel_booking.model.Room;
import com.hotelbooking.hotel_booking.model.User;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.RoomRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final UserRepository users;

    public BookingService(BookingRepository bookings, RoomRepository rooms, UserRepository users) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.users = users;
    }

    @Transactional
    public Booking book(String username, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ServiceException("Check-out must be after check-in.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ServiceException("Check-in date cannot be in the past.");
        }

        User user = users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found."));
        Room room = rooms.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new NotFoundException("Room " + roomNumber + " not found."));

        if (bookings.countOverlapping(room, checkIn, checkOut) > 0) {
            throw new ConflictException("Room is already booked for these dates.");
        }

        return bookings.save(new Booking(user, room, checkIn, checkOut));
    }

    @Transactional
    public void cancel(Long bookingId, String username) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found."));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = booking.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("You cannot cancel this booking.");
        }

        bookings.delete(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> findForUser(String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return bookings.findByUserOrderByCheckInDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookings.findAllByOrderByCheckInDesc();
    }
}