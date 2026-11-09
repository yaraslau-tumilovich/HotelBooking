package com.hotelbooking.hotel_booking.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Integer roomNumber,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut) {
}