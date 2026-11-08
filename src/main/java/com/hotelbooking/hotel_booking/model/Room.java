package com.hotelbooking.hotel_booking.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    private int roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private boolean balcony;

    protected Room() {
    }

    public Room(int roomNumber, RoomType type, BigDecimal pricePerNight, boolean balcony) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.balcony = balcony;
    }

    public Long getId() {
        return id;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public boolean hasBalcony() {
        return balcony;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}