package com.hotelbooking.hotel_booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hotelbooking.hotel_booking.model.Role;
import com.hotelbooking.hotel_booking.model.User;
import com.hotelbooking.hotel_booking.repository.UserRepository;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminPassword;

    public AdminInitializer(UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.password:admin123}") String adminPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!users.existsByUsername("admin")) {
            users.save(new User("admin", encoder.encode(adminPassword), Role.ADMIN));
        }
    }
}