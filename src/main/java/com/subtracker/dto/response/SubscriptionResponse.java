package com.subtracker.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        String serviceName,
        BigDecimal amount,
        String billingCycle,
        LocalDate nextBillingDate,
        boolean active,
        boolean usedRecently
) {}