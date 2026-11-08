package com.hotelbooking.hotel_booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelbooking.hotel_booking.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(int roomNumber);

    boolean existsByRoomNumber(int roomNumber);

    @Query("""
            select r from Room r
            where not exists (
                select b from Booking b
                where b.room = r
                  and b.checkIn < :checkOut
                  and b.checkOut > :checkIn
            )
            order by r.roomNumber
            """)
    List<Room> findAvailable(@Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}