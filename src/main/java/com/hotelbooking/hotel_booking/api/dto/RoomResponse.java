package com.hotelbooking.hotel_booking.api.dto;

import java.math.BigDecimal;

import com.hotelbooking.hotel_booking.model.Room;
import com.hotelbooking.hotel_booking.model.RoomType;

public record RoomResponse(int roomNumber, RoomType type, BigDecimal pricePerNight, boolean balcony) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getRoomNumber(),
                room.getType(),
                room.getPricePerNight(),
                room.hasBalcony());
    }
}