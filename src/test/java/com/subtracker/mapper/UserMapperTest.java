package com.subtracker.mapper;

import com.subtracker.dto.response.UserResponse;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toResponse_mapsAllFieldsCorrectly_forCustomer() {
        User user = User.builder()
                .id(1L)
                .fullName("Jordan Rivera")
                .email("jordan@example.com")
                .password("hashed-password")
                .role(Role.CUSTOMER)
                .build();

        UserResponse response = UserMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fullName()).isEqualTo("Jordan Rivera");
        assertThat(response.email()).isEqualTo("jordan@example.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    void toResponse_mapsRoleAsString_forAdmin() {
        User admin = User.builder()
                .id(2L)
                .fullName("System Admin")
                .email("admin@subtracker.com")
                .password("hashed-password")
                .role(Role.ADMIN)
                .build();

        UserResponse response = UserMapper.toResponse(admin);

        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void toResponse_neverExposesPassword() {
        User user = User.builder()
                .id(3L)
                .fullName("Jordan Rivera")
                .email("jordan@example.com")
                .password("super-secret-hash")
                .role(Role.CUSTOMER)
                .build();

        UserResponse response = UserMapper.toResponse(user);

        // UserResponse has no password field at all — this confirms the DTO shape itself
        assertThat(response.toString()).doesNotContain("super-secret-hash");
    }
}