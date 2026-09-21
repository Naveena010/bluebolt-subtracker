package com.subtracker.service;

import com.subtracker.dto.response.NotificationResponse;
import com.subtracker.entity.Notification;
import com.subtracker.exception.ResourceNotFoundException;
import com.subtracker.exception.UnauthorizedActionException;
import com.subtracker.mapper.NotificationMapper;
import com.subtracker.repository.NotificationRepository;
import com.subtracker.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getMyNotifications() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUser().getId();
        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You are not allowed to access this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return NotificationMapper.toResponse(notification);
    }
}