package com.subtracker.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalMoneySaved,
        long activeUsersCount,
        long totalSubscriptionsTracked,
        long totalCancelledSubscriptions,
        List<ServiceCancelCount> mostCancelledServices
) {}