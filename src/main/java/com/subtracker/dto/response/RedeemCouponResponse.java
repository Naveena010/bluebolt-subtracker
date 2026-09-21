package com.subtracker.dto.response;

import java.math.BigDecimal;

public record RedeemCouponResponse(
        String couponCode,
        Double discountPercentage,
        BigDecimal originalAmount,
        BigDecimal discountedAmount,
        String message
) {}