package com.subtracker.mapper;

import com.subtracker.dto.response.NotificationResponse;
import com.subtracker.entity.Notification;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getSubscription() != null ? notification.getSubscription().getId() : null,
                notification.getSubscription() != null ? notification.getSubscription().getServiceName() : null
        );
    }
}