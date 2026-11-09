package com.hotelbooking.hotel_booking.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel_booking.api.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.api.dto.UserResponse;
import com.hotelbooking.hotel_booking.model.User;
import com.hotelbooking.hotel_booking.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;

    public AuthApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request.username(), request.password());
        return new UserResponse(user.getUsername(), user.getRole());
    }
}