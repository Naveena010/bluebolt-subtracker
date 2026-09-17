package com.subtracker.mapper;

import com.subtracker.dto.response.UserResponse;
import com.subtracker.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}