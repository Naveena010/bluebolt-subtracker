package com.subtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subtracker.config.SecurityConfig;
import com.subtracker.dto.request.RegisterRequest;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import com.subtracker.repository.UserRepository;
import com.subtracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BootstrapController.class)
@Import(SecurityConfig.class)
class BootstrapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void createFirstAdmin_returns200_whenNoAdminExistsYet() throws Exception {
        RegisterRequest request = new RegisterRequest("System Admin", "admin@subtracker.com", "Admin@123");

        when(userRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(jwtUtil.generateToken(any(), any())).thenReturn("fake-admin-token");

        mockMvc.perform(post("/api/bootstrap/first-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-admin-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void createFirstAdmin_returns403_whenAdminAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Another Admin", "admin2@subtracker.com", "Admin@123");

        User existingAdmin = User.builder().id(1L).email("admin@subtracker.com").role(Role.ADMIN).build();
        when(userRepository.findAll()).thenReturn(List.of(existingAdmin));

        mockMvc.perform(post("/api/bootstrap/first-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("An admin already exists. This endpoint is now locked."));
    }
}