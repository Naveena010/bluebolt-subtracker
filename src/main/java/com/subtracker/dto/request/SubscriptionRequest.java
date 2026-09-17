package com.subtracker.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequest(
        @NotBlank(message = "Service name is required") String serviceName,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        @NotBlank(message = "Billing cycle is required") String billingCycle,
        @NotNull(message = "Next billing date is required") @Future(message = "Next billing date must be in the future") LocalDate nextBillingDate
) {}