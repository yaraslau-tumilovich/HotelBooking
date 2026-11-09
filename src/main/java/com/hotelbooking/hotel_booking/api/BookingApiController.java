package com.hotelbooking.hotel_booking.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel_booking.api.dto.BookingResponse;
import com.hotelbooking.hotel_booking.api.dto.CreateBookingRequest;
import com.hotelbooking.hotel_booking.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private final BookingService bookingService;

    public BookingApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody CreateBookingRequest request, Principal principal) {
        return BookingResponse.from(bookingService.book(
                principal.getName(), request.roomNumber(), request.checkIn(), request.checkOut()));
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(Principal principal) {
        return bookingService.findForUser(principal.getName()).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @GetMapping
    public List<BookingResponse> allBookings() {
        return bookingService.findAll().stream().map(BookingResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, Principal principal) {
        bookingService.cancel(id, principal.getName());
    }
}