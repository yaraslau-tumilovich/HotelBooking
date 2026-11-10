package com.hotelbooking.hotel_booking.api;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;

final class ApiTestSupport {

    private ApiTestSupport() {
    }

    static String basic(String username, String password) {
        return "Basic " + HttpHeaders.encodeBasicAuth(username, password, StandardCharsets.UTF_8);
    }
}