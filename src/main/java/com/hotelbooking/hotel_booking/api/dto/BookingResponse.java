package com.hotelbooking.hotel_booking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hotelbooking.hotel_booking.model.Booking;

public record BookingResponse(
        Long id,
        int roomNumber,
        String username,
        LocalDate checkIn,
        LocalDate checkOut,
        long nights,
        BigDecimal totalPrice) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getRoomNumber(),
                booking.getUser().getUsername(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getNights(),
                booking.getTotalPrice());
    }
}