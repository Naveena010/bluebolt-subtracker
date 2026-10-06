package com.subtracker.mapper;

import com.subtracker.dto.response.NotificationResponse;
import com.subtracker.entity.Notification;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private final User customer = User.builder()
            .id(1L)
            .fullName("Jordan Rivera")
            .email("jordan@example.com")
            .role(Role.CUSTOMER)
            .build();

    @Test
    void toResponse_mapsAllFields_whenSubscriptionIsPresent() {
        Subscription subscription = Subscription.builder()
                .id(10L)
                .serviceName("Netflix")
                .amount(BigDecimal.valueOf(499))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.now().plusDays(3))
                .active(true)
                .usedRecently(false)
                .user(customer)
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        Notification notification = Notification.builder()
                .id(50L)
                .message("You haven't used Netflix recently...")
                .isRead(false)
                .createdAt(createdAt)
                .user(customer)
                .subscription(subscription)
                .build();

        NotificationResponse response = NotificationMapper.toResponse(notification);

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.message()).isEqualTo("You haven't used Netflix recently...");
        assertThat(response.isRead()).isFalse();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.subscriptionId()).isEqualTo(10L);
        assertThat(response.serviceName()).isEqualTo("Netflix");
    }

    @Test
    void toResponse_handlesNullSubscription_withoutThrowing() {
        Notification notification = Notification.builder()
                .id(51L)
                .message("General notification")
                .isRead(true)
                .createdAt(LocalDateTime.now())
                .user(customer)
                .subscription(null)
                .build();

        NotificationResponse response = NotificationMapper.toResponse(notification);

        assertThat(response.subscriptionId()).isNull();
        assertThat(response.serviceName()).isNull();
    }

    @Test
    void toResponse_reflectsReadState() {
        Notification readNotification = Notification.builder()
                .id(52L)
                .message("Already seen this")
                .isRead(true)
                .createdAt(LocalDateTime.now())
                .user(customer)
                .build();

        NotificationResponse response = NotificationMapper.toResponse(readNotification);

        assertThat(response.isRead()).isTrue();
    }
}