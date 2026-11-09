package com.hotelbooking.hotel_booking.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.model.Room;
import com.hotelbooking.hotel_booking.service.RoomService;

@Controller
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public String rooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Model model) {

        List<Room> rooms;
        boolean searched = false;

        if (checkIn != null && checkOut != null) {
            try {
                rooms = roomService.findAvailable(checkIn, checkOut);
                searched = true;
                model.addAttribute("checkIn", checkIn);
                model.addAttribute("checkOut", checkOut);
                model.addAttribute("nights",
                        BigDecimal.valueOf(ChronoUnit.DAYS.between(checkIn, checkOut)));
            } catch (ServiceException e) {
                model.addAttribute("error", e.getMessage());
                rooms = roomService.findAll();
            }
        } else {
            rooms = roomService.findAll();
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("searched", searched);
        return "rooms";
    }
}