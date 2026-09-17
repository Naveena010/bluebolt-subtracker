package com.subtracker.dto.response;

import java.time.LocalDate;

public record CouponResponse(
        Long id,
        String code,
        Double discountPercentage,
        LocalDate expiryDate,
        boolean active,
        String createdByEmail
) {}