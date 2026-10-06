package com.subtracker.mapper;

import com.subtracker.dto.request.CouponRequest;
import com.subtracker.dto.response.CouponResponse;
import com.subtracker.entity.Coupon;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    private final User admin = User.builder()
            .id(1L)
            .fullName("System Admin")
            .email("admin@subtracker.com")
            .role(Role.ADMIN)
            .build();

    @Test
    void toEntity_mapsRequestFieldsAndAttachesCreatedBy() {
        CouponRequest request = new CouponRequest("SAVE20", 20.0, LocalDate.now().plusDays(30));

        Coupon coupon = CouponMapper.toEntity(request, admin);

        assertThat(coupon.getCode()).isEqualTo("SAVE20");
        assertThat(coupon.getDiscountPercentage()).isEqualTo(20.0);
        assertThat(coupon.getCreatedBy()).isEqualTo(admin);
    }

    @Test
    void toEntity_defaultsActiveToTrue() {
        CouponRequest request = new CouponRequest("SAVE10", 10.0, LocalDate.now().plusDays(15));

        Coupon coupon = CouponMapper.toEntity(request, admin);

        assertThat(coupon.isActive()).isTrue();
    }

    @Test
    void toResponse_mapsAllFieldsIncludingCreatedByEmail() {
        LocalDate expiry = LocalDate.now().plusDays(30);

        Coupon coupon = Coupon.builder()
                .id(100L)
                .code("SAVE20")
                .discountPercentage(20.0)
                .expiryDate(expiry)
                .active(true)
                .createdBy(admin)
                .build();

        CouponResponse response = CouponMapper.toResponse(coupon);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.code()).isEqualTo("SAVE20");
        assertThat(response.discountPercentage()).isEqualTo(20.0);
        assertThat(response.expiryDate()).isEqualTo(expiry);
        assertThat(response.active()).isTrue();
        assertThat(response.createdByEmail()).isEqualTo("admin@subtracker.com");
    }

    @Test
    void toResponse_handlesNullCreatedBy_withoutThrowing() {
        Coupon coupon = Coupon.builder()
                .id(101L)
                .code("NOOWNER")
                .discountPercentage(15.0)
                .expiryDate(LocalDate.now().plusDays(10))
                .active(true)
                .createdBy(null)
                .build();

        CouponResponse response = CouponMapper.toResponse(coupon);

        assertThat(response.createdByEmail()).isNull();
    }

    @Test
    void toResponse_reflectsDeactivatedState() {
        Coupon deactivated = Coupon.builder()
                .id(102L)
                .code("EXPIRED5")
                .discountPercentage(5.0)
                .expiryDate(LocalDate.now().plusDays(10))
                .active(false)
                .createdBy(admin)
                .build();

        CouponResponse response = CouponMapper.toResponse(deactivated);

        assertThat(response.active()).isFalse();
    }
}