package com.hotelbooking.hotel_booking.api.dto;

import com.hotelbooking.hotel_booking.model.Role;

public record UserResponse(String username, Role role) {
}