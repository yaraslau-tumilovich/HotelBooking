package com.hotelbooking.hotel_booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelbooking.hotel_booking.model.Booking;
import com.hotelbooking.hotel_booking.model.Room;
import com.hotelbooking.hotel_booking.model.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByCheckInDesc(User user);

    @Query("""
            select count(b) from Booking b
            where b.room = :room
              and b.checkIn < :checkOut
              and b.checkOut > :checkIn
            """)
    long countOverlapping(@Param("room") Room room,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}