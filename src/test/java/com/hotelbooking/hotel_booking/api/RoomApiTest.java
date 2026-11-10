package com.hotelbooking.hotel_booking.api;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import static com.hotelbooking.hotel_booking.api.ApiTestSupport.basic;
import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.service.RoomService;
import com.hotelbooking.hotel_booking.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoomApiTest {

    private static final String NEW_ROOM = "{\"roomNumber\":401,\"type\":\"DELUXE\",\"pricePerNight\":210.00,\"balcony\":true}";

    @Autowired
    MockMvc mvc;
    @Autowired
    UserService userService;
    @Autowired
    RoomService roomService;

    @BeforeEach
    void setUp() {
        userService.register("guest", "guest123");
        roomService.addRoom(101, RoomType.STANDARD, new BigDecimal("59.00"), false);
    }

    @Test
    void anyoneCanListRooms() throws Exception {
        mvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"roomNumber\":101")));
    }

    @Test
    void invalidDateRangeReturns400() throws Exception {
        mvc.perform(get("/api/rooms/available")
                .param("checkIn", "2026-12-10")
                .param("checkOut", "2026-12-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCannotCreateRoom() throws Exception {
        mvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ROOM))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotCreateRoom() throws Exception {
        mvc.perform(post("/api/rooms")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ROOM))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateRoom() throws Exception {
        mvc.perform(post("/api/rooms")
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ROOM))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"roomNumber\":401")));
    }

    @Test
    void duplicateRoomReturns409() throws Exception {
        mvc.perform(post("/api/rooms")
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomNumber\":101,\"type\":\"STANDARD\",\"pricePerNight\":50.00,\"balcony\":false}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRoomReturns400WithFieldNames() throws Exception {
        mvc.perform(post("/api/rooms")
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomNumber\":-5,\"type\":\"DELUXE\",\"pricePerNight\":0,\"balcony\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("roomNumber")))
                .andExpect(content().string(containsString("pricePerNight")));
    }

    @Test
    void adminCanChangePrice() throws Exception {
        mvc.perform(put("/api/rooms/101/price")
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pricePerNight\":75.00}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("75.00")));
    }

    @Test
    void deletingUnknownRoomReturns404() throws Exception {
        mvc.perform(delete("/api/rooms/999")
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}