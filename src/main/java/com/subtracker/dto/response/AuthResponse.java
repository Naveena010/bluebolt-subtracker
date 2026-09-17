package com.subtracker.dto.response;

public record AuthResponse(
        String token,
        String email,
        String role
) {}