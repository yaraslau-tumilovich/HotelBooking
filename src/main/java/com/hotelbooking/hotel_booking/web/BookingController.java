package com.hotelbooking.hotel_booking.web;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hotelbooking.hotel_booking.exception.ServiceException;
import com.hotelbooking.hotel_booking.service.BookingService;

@Controller
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public String book(
            @RequestParam int roomNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Principal principal,
            RedirectAttributes redirect) {

        try {
            bookingService.book(principal.getName(), roomNumber, checkIn, checkOut);
            redirect.addFlashAttribute("success",
                    "Room " + roomNumber + " booked: " + checkIn + " to " + checkOut + ".");
            return "redirect:/my-bookings";
        } catch (ServiceException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/rooms?checkIn=" + checkIn + "&checkOut=" + checkOut;
        }
    }

    @GetMapping("/my-bookings")
    public String myBookings(Principal principal, Model model) {
        model.addAttribute("bookings", bookingService.findForUser(principal.getName()));
        return "my-bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            bookingService.cancel(id, principal.getName());
            redirect.addFlashAttribute("success", "Booking cancelled.");
        } catch (ServiceException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-bookings";
    }
}