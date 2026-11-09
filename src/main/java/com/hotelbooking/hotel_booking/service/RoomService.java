package com.hotelbooking.hotel_booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Room;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.RoomRepository;

@Service
public class RoomService {

    private final RoomRepository rooms;
    private final BookingRepository bookings;

    public RoomService(RoomRepository rooms, BookingRepository bookings) {
        this.rooms = rooms;
        this.bookings = bookings;
    }

    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return rooms.findAll();
    }

    @Transactional(readOnly = true)
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ServiceException("Check-out must be after check-in.");
        }
        return rooms.findAvailable(checkIn, checkOut);
    }

    @Transactional
    public Room addRoom(int number, RoomType type, BigDecimal price, boolean balcony) {
        if (number <= 0) {
            throw new ServiceException("Room number must be positive.");
        }
        if (price == null || price.signum() <= 0) {
            throw new ServiceException("Price must be greater than zero.");
        }
        if (rooms.existsByRoomNumber(number)) {
            throw new ServiceException("Room " + number + " already exists.");
        }
        return rooms.save(new Room(number, type, price, balcony));
    }

    @Transactional
    public void changePrice(int number, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ServiceException("Price must be greater than zero.");
        }
        Room room = getByNumber(number);
        room.setPricePerNight(price);
    }

    @Transactional
    public void removeRoom(int number) {
        Room room = getByNumber(number);
        if (bookings.existsByRoom(room)) {
            throw new ServiceException("Room " + number + " has bookings and cannot be removed.");
        }
        rooms.delete(room);
    }

    private Room getByNumber(int number) {
        return rooms.findByRoomNumber(number)
                .orElseThrow(() -> new ServiceException("Room " + number + " not found."));
    }
}