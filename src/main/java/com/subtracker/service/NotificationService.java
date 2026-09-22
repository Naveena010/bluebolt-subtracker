package com.subtracker.service;

import com.subtracker.dto.response.NotificationResponse;
import com.subtracker.dto.response.PageResponse;
import com.subtracker.entity.Notification;
import com.subtracker.exception.ResourceNotFoundException;
import com.subtracker.exception.UnauthorizedActionException;
import com.subtracker.mapper.NotificationMapper;
import com.subtracker.repository.NotificationRepository;
import com.subtracker.security.SecurityUtil;
import com.subtracker.util.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public PageResponse<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageMapper.toPageResponse(notifications, NotificationMapper::toResponse);
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