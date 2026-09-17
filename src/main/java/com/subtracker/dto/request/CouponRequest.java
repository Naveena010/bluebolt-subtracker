package com.subtracker.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CouponRequest(
        @NotBlank(message = "Coupon code is required") String code,
        @NotNull(message = "Discount percentage is required")
        @DecimalMin(value = "1.0", message = "Discount must be at least 1%")
        @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%") Double discountPercentage,
        @NotNull(message = "Expiry date is required") @Future(message = "Expiry date must be in the future") LocalDate expiryDate
) {}