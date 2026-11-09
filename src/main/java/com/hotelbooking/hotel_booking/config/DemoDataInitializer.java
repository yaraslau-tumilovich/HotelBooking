package com.hotelbooking.hotel_booking.config;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.service.RoomService;

@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private final RoomService rooms;

    public DemoDataInitializer(RoomService rooms) {
        this.rooms = rooms;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!rooms.findAll().isEmpty()) {
            return;
        }
        rooms.addRoom(101, RoomType.STANDARD, new BigDecimal("59.00"), false);
        rooms.addRoom(102, RoomType.STANDARD, new BigDecimal("65.00"), false);
        rooms.addRoom(201, RoomType.STANDARD, new BigDecimal("79.00"), true);
        rooms.addRoom(202, RoomType.DELUXE, new BigDecimal("119.00"), true);
        rooms.addRoom(301, RoomType.DELUXE, new BigDecimal("149.00"), true);
        rooms.addRoom(302, RoomType.DELUXE, new BigDecimal("189.00"), true);
    }
}