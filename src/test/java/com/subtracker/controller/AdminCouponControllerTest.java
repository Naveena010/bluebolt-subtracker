package com.subtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subtracker.config.SecurityConfig;
import com.subtracker.dto.request.CouponRequest;
import com.subtracker.dto.response.CouponResponse;
import com.subtracker.repository.UserRepository;
import com.subtracker.security.JwtUtil;
import com.subtracker.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCouponController.class)
@Import(SecurityConfig.class)
class AdminCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    private CouponResponse sampleCoupon() {
        return new CouponResponse(100L, "SAVE20", 20.0, LocalDate.now().plusDays(30), true, "admin@subtracker.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCoupon_returns200_whenAuthenticatedAsAdmin() throws Exception {
        CouponRequest request = new CouponRequest("SAVE20", 20.0, LocalDate.now().plusDays(30));

        when(couponService.createCoupon(any())).thenReturn(sampleCoupon());

        mockMvc.perform(post("/api/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE20"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createCoupon_returns403_whenAuthenticatedAsCustomer() throws Exception {
        CouponRequest request = new CouponRequest("SAVE20", 20.0, LocalDate.now().plusDays(30));

        mockMvc.perform(post("/api/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCoupon_returns401_whenNotAuthenticated() throws Exception {
        CouponRequest request = new CouponRequest("SAVE20", 20.0, LocalDate.now().plusDays(30));

        mockMvc.perform(post("/api/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCoupon_returns400_whenDiscountExceeds100() throws Exception {
        String invalidJson = """
                {
                  "code": "SAVE200",
                  "discountPercentage": 200.0,
                  "expiryDate": "2026-12-31"
                }
                """;

        mockMvc.perform(post("/api/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCoupons_returns200AndList() throws Exception {
        when(couponService.getAllCoupons()).thenReturn(List.of(sampleCoupon()));

        mockMvc.perform(get("/api/admin/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE20"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateCoupon_returns200() throws Exception {
        CouponResponse deactivated = new CouponResponse(100L, "SAVE20", 20.0, LocalDate.now().plusDays(30), false, "admin@subtracker.com");

        when(couponService.deactivateCoupon(100L)).thenReturn(deactivated);

        mockMvc.perform(patch("/api/admin/coupons/100/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}