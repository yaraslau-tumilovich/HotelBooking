package com.hotelbooking.hotel_booking.api.dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public record ApiErrorResponse(int status, String error, String message, LocalDateTime timestamp) {

    public static ApiErrorResponse of(HttpStatus status, String message) {
        return new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now());
    }
}