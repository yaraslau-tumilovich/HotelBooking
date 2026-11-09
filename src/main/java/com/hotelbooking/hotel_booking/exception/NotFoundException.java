package com.hotelbooking.hotel_booking.exception;

public class NotFoundException extends ServiceException {

    public NotFoundException(String message) {
        super(message);
    }
}