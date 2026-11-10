package com.hotelbooking.hotel_booking.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import static com.hotelbooking.hotel_booking.api.ApiTestSupport.basic;
import com.hotelbooking.hotel_booking.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserService userService;

    private static final String REGISTER_URL = "/api/auth/register";

    @Test
    void registrationReturns201WithoutExposingPassword() throws Exception {
        mvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newbie\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("newbie")))
                .andExpect(content().string(not(containsString("secret123"))))
                .andExpect(content().string(not(containsString("assword"))));
    }

    @Test
    void duplicateUsernameReturns409() throws Exception {
        userService.register("guest", "guest123");

        mvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"guest\",\"password\":\"another123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shortCredentialsReturn400() throws Exception {
        mvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newUserCanCallProtectedEndpoint() throws Exception {
        userService.register("guest", "guest123");

        mvc.perform(get("/api/bookings/my")
                .header(HttpHeaders.AUTHORIZATION, basic("guest", "guest123")))
                .andExpect(status().isOk());
    }
}