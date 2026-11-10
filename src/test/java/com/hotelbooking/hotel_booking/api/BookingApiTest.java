package com.hotelbooking.hotel_booking.api;

import static com.hotelbooking.hotel_booking.api.ApiTestSupport.basic;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_booking.model.RoomType;
import com.hotelbooking.hotel_booking.service.BookingService;
import com.hotelbooking.hotel_booking.service.RoomService;
import com.hotelbooking.hotel_booking.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingApiTest {

    private final LocalDate checkIn = LocalDate.now().plusDays(30);
    private final LocalDate checkOut = checkIn.plusDays(3);

    @Autowired
    MockMvc mvc;
    @Autowired
    UserService userService;
    @Autowired
    RoomService roomService;
    @Autowired
    BookingService bookingService;

    @BeforeEach
    void setUp() {
        userService.register("guest", "guest123");
        userService.register("guest2", "guest123");
        roomService.addRoom(101, RoomType.STANDARD, new BigDecimal("59.00"), false);
    }

    private String bookingJson(int room, LocalDate in, LocalDate out) {
        return "{\"roomNumber\":" + room
                + ",\"checkIn\":\"" + in
                + "\",\"checkOut\":\"" + out + "\"}";
    }

    @Test
    void bookingRequiresLogin() throws Exception {
        mvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(101, checkIn, checkOut)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mvc.perform(get("/api/bookings/my")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanBookRoomAndSeesTotalPrice() throws Exception {
        mvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(101, checkIn, checkOut)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"nights\":3")))
                .andExpect(content().string(containsString("177.00")));
    }

    @Test
    void overlappingBookingReturns409() throws Exception {
        bookingService.book("guest", 101, checkIn, checkOut);

        mvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("guest2", "guest123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(101, checkIn.plusDays(1), checkOut.plusDays(1))))
                .andExpect(status().isConflict());
    }

    @Test
    void checkoutBeforeCheckinReturns400() throws Exception {
        mvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(101, checkOut, checkIn)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingFieldsReturn400() throws Exception {
        mvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomNumber\":101}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownRoomReturns404() throws Exception {
        mvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(999, checkIn, checkOut)))
                .andExpect(status().isNotFound());
    }

    @Test
    void myBookingsShowOnlyOwnBookings() throws Exception {
        bookingService.book("guest", 101, checkIn, checkOut);

        mvc.perform(get("/api/bookings/my")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"roomNumber\":101")));

        mvc.perform(get("/api/bookings/my")
                .header(HttpHeaders.AUTHORIZATION, basic("guest2", "guest123")))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void onlyAdminCanListAllBookings() throws Exception {
        mvc.perform(get("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void cannotCancelSomeoneElsesBooking() throws Exception {
        Long id = bookingService.book("guest", 101, checkIn, checkOut).getId();

        mvc.perform(delete("/api/bookings/" + id)
                .header(HttpHeaders.AUTHORIZATION, basic("guest2", "guest123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanCancelOnceAndThenGets404() throws Exception {
        Long id = bookingService.book("guest", 101, checkIn, checkOut).getId();

        mvc.perform(delete("/api/bookings/" + id)
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123")))
                .andExpect(status().isNoContent());

        mvc.perform(delete("/api/bookings/" + id)
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCancelAnyBooking() throws Exception {
        Long id = bookingService.book("guest", 101, checkIn, checkOut).getId();

        mvc.perform(delete("/api/bookings/" + id)
                .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }
}