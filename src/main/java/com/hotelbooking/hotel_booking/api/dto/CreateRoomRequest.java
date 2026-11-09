package com.hotelbooking.hotel_booking.api.dto;

import java.math.BigDecimal;

import com.hotelbooking.hotel_booking.model.RoomType;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRoomRequest(
        @NotNull @Positive Integer roomNumber,
        @NotNull RoomType type,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal pricePerNight,
        boolean balcony) {
}