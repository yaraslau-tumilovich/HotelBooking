package com.hotelbooking.hotel_booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel_booking.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}