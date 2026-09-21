package com.subtracker.controller;

import com.subtracker.dto.request.RegisterRequest;
import com.subtracker.dto.response.AuthResponse;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import com.subtracker.exception.UnauthorizedActionException;
import com.subtracker.repository.UserRepository;
import com.subtracker.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bootstrap")
@RequiredArgsConstructor
public class BootstrapController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/first-admin")
    public ResponseEntity<AuthResponse> createFirstAdmin(@Valid @RequestBody RegisterRequest request) {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == Role.ADMIN);

        if (adminExists) {
            throw new UnauthorizedActionException("An admin already exists. This endpoint is now locked.");
        }

        User admin = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, admin.getEmail(), admin.getRole().name()));
    }
}