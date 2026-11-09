package com.hotelbooking.hotel_booking.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel_booking.api.dto.ChangePriceRequest;
import com.hotelbooking.hotel_booking.api.dto.CreateRoomRequest;
import com.hotelbooking.hotel_booking.api.dto.RoomResponse;
import com.hotelbooking.hotel_booking.service.RoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomService roomService;

    public RoomApiController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> list() {
        return roomService.findAll().stream().map(RoomResponse::from).toList();
    }

    @GetMapping("/available")
    public List<RoomResponse> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return roomService.findAvailable(checkIn, checkOut).stream().map(RoomResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody CreateRoomRequest request) {
        return RoomResponse.from(roomService.addRoom(
                request.roomNumber(), request.type(), request.pricePerNight(), request.balcony()));
    }

    @PutMapping("/{number}/price")
    public RoomResponse changePrice(@PathVariable int number,
            @Valid @RequestBody ChangePriceRequest request) {
        return RoomResponse.from(roomService.changePrice(number, request.pricePerNight()));
    }

    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int number) {
        roomService.removeRoom(number);
    }
}