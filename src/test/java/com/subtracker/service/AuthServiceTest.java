package com.subtracker.service;

import com.subtracker.dto.request.LoginRequest;
import com.subtracker.dto.request.RegisterRequest;
import com.subtracker.dto.response.AuthResponse;
import com.subtracker.dto.response.UserResponse;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import com.subtracker.exception.DuplicateResourceException;
import com.subtracker.exception.InvalidCredentialsException;
import com.subtracker.repository.UserRepository;
import com.subtracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Jordan Rivera", "jordan@example.com", "password123");
        loginRequest = new LoginRequest("jordan@example.com", "password123");

        existingUser = User.builder()
                .id(1L)
                .fullName("Jordan Rivera")
                .email("jordan@example.com")
                .password("hashed-password")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    void register_createsCustomerAccount_whenEmailIsNew() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashed-password");

        UserResponse response = authService.register(registerRequest);

        assertThat(response.email()).isEqualTo("jordan@example.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        verify(userRepository).save(argThat(user -> user.getRole() == Role.CUSTOMER));
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenAndRole_whenCredentialsAreValid() {
        when(userRepository.findByEmail("jordan@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken("jordan@example.com", "CUSTOMER")).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        assertThat(response.email()).isEqualTo("jordan@example.com");
    }

    @Test
    void login_throwsInvalidCredentialsException_whenEmailNotFound() {
        when(userRepository.findByEmail("jordan@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_throwsInvalidCredentialsException_whenPasswordIsWrong() {
        when(userRepository.findByEmail("jordan@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }
}