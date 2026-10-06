package com.subtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subtracker.config.SecurityConfig;
import com.subtracker.dto.request.SubscriptionRequest;
import com.subtracker.dto.request.UsageUpdateRequest;
import com.subtracker.dto.response.PageResponse;
import com.subtracker.dto.response.SubscriptionResponse;
import com.subtracker.exception.ResourceNotFoundException;
import com.subtracker.exception.UnauthorizedActionException;
import com.subtracker.repository.UserRepository;
import com.subtracker.security.JwtUtil;
import com.subtracker.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@Import(SecurityConfig.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    private SubscriptionResponse sampleResponse() {
        return new SubscriptionResponse(
                10L, "Netflix", BigDecimal.valueOf(499), "MONTHLY",
                LocalDate.now().plusDays(5), true, true
        );
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void addSubscription_returns200_whenAuthenticatedAsCustomer() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                "Netflix", BigDecimal.valueOf(499), "MONTHLY", LocalDate.now().plusDays(5));

        when(subscriptionService.addSubscription(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/customer/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("Netflix"));
    }

    @Test
    void addSubscription_returns401_whenNotAuthenticated() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                "Netflix", BigDecimal.valueOf(499), "MONTHLY", LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/customer/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addSubscription_returns403_whenAuthenticatedAsAdminInsteadOfCustomer() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                "Netflix", BigDecimal.valueOf(499), "MONTHLY", LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/customer/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getMySubscriptions_returns200AndPagedList() throws Exception {
        PageResponse<SubscriptionResponse> page =
                new PageResponse<>(List.of(sampleResponse()), 0, 10, 1, 1, true);

        when(subscriptionService.getMySubscriptions(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/customer/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].serviceName").value("Netflix"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateUsage_returns200_whenSubscriptionBelongsToCaller() throws Exception {
        when(subscriptionService.updateUsageStatus(anyLong(), any())).thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/customer/subscriptions/10/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsageUpdateRequest(false))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateUsage_returns403_whenSubscriptionBelongsToAnotherUser() throws Exception {
        when(subscriptionService.updateUsageStatus(anyLong(), any()))
                .thenThrow(new UnauthorizedActionException("You are not allowed to access this subscription"));

        mockMvc.perform(patch("/api/customer/subscriptions/10/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsageUpdateRequest(false))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getSubscriptionById_returns404_whenNotFound() throws Exception {
        when(subscriptionService.getSubscriptionById(999L))
                .thenThrow(new ResourceNotFoundException("Subscription not found with id: 999"));

        mockMvc.perform(get("/api/customer/subscriptions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteSubscription_returns200_whenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/customer/subscriptions/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription deleted successfully"));
    }
}