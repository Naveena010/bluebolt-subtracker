package com.subtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RedeemCouponRequest(
        @NotBlank(message = "Coupon code is required") String couponCode,
        @NotNull(message = "Subscription id is required") Long subscriptionId
) {}