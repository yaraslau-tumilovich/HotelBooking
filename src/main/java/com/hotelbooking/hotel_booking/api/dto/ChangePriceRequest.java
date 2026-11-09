package com.hotelbooking.hotel_booking.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangePriceRequest(
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal pricePerNight) {
}