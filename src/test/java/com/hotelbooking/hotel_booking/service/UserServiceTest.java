package com.hotelbooking.hotel_booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Role;
import com.hotelbooking.hotel_booking.model.User;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder encoder;

    @Test
    void registerStoresHashedPassword() {
        User user = userService.register("alice", "secret123");

        assertEquals(Role.USER, user.getRole());
        assertNotEquals("secret123", user.getPasswordHash());
        assertTrue(encoder.matches("secret123", user.getPasswordHash()));
    }

    @Test
    void duplicateUsernameIsRejected() {
        userService.register("alice", "secret123");

        assertThrows(ServiceException.class,
                () -> userService.register("alice", "other-pass"));
    }

    @Test
    void shortPasswordIsRejected() {
        assertThrows(ServiceException.class,
                () -> userService.register("alice", "123"));
    }

    @Test
    void shortUsernameIsRejected() {
        assertThrows(ServiceException.class,
                () -> userService.register("al", "secret123"));
    }
}