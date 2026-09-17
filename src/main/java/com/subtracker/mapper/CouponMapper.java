package com.subtracker.mapper;

import com.subtracker.dto.request.CouponRequest;
import com.subtracker.dto.response.CouponResponse;
import com.subtracker.entity.Coupon;
import com.subtracker.entity.User;

public class CouponMapper {

    public static Coupon toEntity(CouponRequest request, User admin) {
        return Coupon.builder()
                .code(request.code())
                .discountPercentage(request.discountPercentage())
                .expiryDate(request.expiryDate())
                .active(true)
                .createdBy(admin)
                .build();
    }

    public static CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountPercentage(),
                coupon.getExpiryDate(),
                coupon.isActive(),
                coupon.getCreatedBy() != null ? coupon.getCreatedBy().getEmail() : null
        );
    }
}