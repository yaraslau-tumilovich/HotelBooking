package com.hotelbooking.hotel_booking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Role;
import com.hotelbooking.hotel_booking.model.User;
import com.hotelbooking.hotel_booking.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public User register(String username, String rawPassword) {
        String name = username == null ? "" : username.trim();

        if (name.length() < 3 || name.length() > 30) {
            throw new ServiceException("Username must be 3-30 characters long.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ServiceException("Password must be at least 6 characters long.");
        }
        if (users.existsByUsername(name)) {
            throw new ServiceException("Username is already taken.");
        }

        return users.save(new User(name, encoder.encode(rawPassword), Role.USER));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ServiceException("User not found."));
    }
}