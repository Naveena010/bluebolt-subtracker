package com.subtracker.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String message,
        boolean isRead,
        LocalDateTime createdAt,
        Long subscriptionId,
        String serviceName
) {}