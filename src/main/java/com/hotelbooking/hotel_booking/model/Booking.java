package com.hotelbooking.hotel_booking.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    protected Booking() {
    }

    public Booking(User user, Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        this.user = user;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }
}