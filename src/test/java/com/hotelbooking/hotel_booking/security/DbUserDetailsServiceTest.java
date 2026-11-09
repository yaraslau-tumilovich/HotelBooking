package com.hotelbooking.hotel_booking.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.service.UserService;

@SpringBootTest
@Transactional
class DbUserDetailsServiceTest {

    @Autowired
    DbUserDetailsService detailsService;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder encoder;

    @Value("${app.admin.password:admin123}")
    String adminPassword;

    private boolean hasAuthority(UserDetails user, String authority) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    @Test
    void defaultAdminIsCreatedOnStartup() {
        UserDetails admin = detailsService.loadUserByUsername("admin");

        assertTrue(hasAuthority(admin, "ROLE_ADMIN"));
        assertTrue(encoder.matches(adminPassword, admin.getPassword()));
    }

    @Test
    void registeredUserGetsRoleUser() {
        userService.register("alice", "secret123");

        UserDetails alice = detailsService.loadUserByUsername("alice");

        assertTrue(hasAuthority(alice, "ROLE_USER"));
        assertFalse(hasAuthority(alice, "ROLE_ADMIN"));
    }

    @Test
    void unknownUserIsRejected() {
        assertThrows(UsernameNotFoundException.class,
                () -> detailsService.loadUserByUsername("nobody"));
    }
}