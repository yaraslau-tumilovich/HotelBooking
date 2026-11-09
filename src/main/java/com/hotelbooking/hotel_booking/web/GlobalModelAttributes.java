package com.hotelbooking.hotel_booking.web;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public String currentUser(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    @ModelAttribute("today")
    public LocalDate today() {
        return LocalDate.now();
    }
}