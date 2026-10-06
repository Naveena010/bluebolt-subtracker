package com.subtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subtracker.config.SecurityConfig;
import com.subtracker.dto.request.LoginRequest;
import com.subtracker.dto.request.RegisterRequest;
import com.subtracker.dto.response.AuthResponse;
import com.subtracker.dto.response.UserResponse;
import com.subtracker.exception.DuplicateResourceException;
import com.subtracker.exception.InvalidCredentialsException;
import com.subtracker.repository.UserRepository;
import com.subtracker.security.JwtUtil;
import com.subtracker.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Mocked so the REAL JwtAuthFilter can be constructed by Spring (do NOT mock JwtAuthFilter itself)
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void register_returns200AndUserResponse_whenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("Jordan Rivera", "jordan@example.com", "password123");
        UserResponse response = new UserResponse(1L, "Jordan Rivera", "jordan@example.com", "CUSTOMER");

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jordan@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_returns400_whenEmailIsBlank() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("Jordan Rivera", "", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Jordan Rivera", "jordan@example.com", "password123");

        when(authService.register(any()))
                .thenThrow(new DuplicateResourceException("Email already registered: jordan@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered: jordan@example.com"));
    }

    @Test
    void login_returns200AndToken_whenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("jordan@example.com", "password123");
        AuthResponse response = new AuthResponse("fake-jwt-token", "jordan@example.com", "CUSTOMER");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void login_returns401_whenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("jordan@example.com", "wrongpassword");

        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}