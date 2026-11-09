package com.hotelbooking.hotel_booking.exception;

public class ForbiddenException extends ServiceException {

    public ForbiddenException(String message) {
        super(message);
    }
}