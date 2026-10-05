package com.subtracker.service;

import com.subtracker.dto.request.SubscriptionRequest;
import com.subtracker.dto.request.UsageUpdateRequest;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import com.subtracker.exception.ResourceNotFoundException;
import com.subtracker.exception.UnauthorizedActionException;
import com.subtracker.repository.SubscriptionRepository;
import com.subtracker.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User owner;
    private User otherUser;
    private Subscription subscription;
    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@example.com").role(Role.CUSTOMER).build();
        otherUser = User.builder().id(2L).email("intruder@example.com").role(Role.CUSTOMER).build();

        subscription = Subscription.builder()
                .id(10L)
                .serviceName("Netflix")
                .amount(BigDecimal.valueOf(499))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.now().plusDays(3))
                .active(true)
                .usedRecently(true)
                .user(owner)
                .build();

        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    void addSubscription_savesSubscriptionForCurrentUser() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(owner);

        SubscriptionRequest request = new SubscriptionRequest(
                "Spotify", BigDecimal.valueOf(199), "MONTHLY", LocalDate.now().plusDays(5));

        subscriptionService.addSubscription(request);

        verify(subscriptionRepository).save(argThat(sub ->
                sub.getServiceName().equals("Spotify") && sub.getUser().equals(owner)
        ));
    }

    @Test
    void updateUsageStatus_marksSubscriptionUnused_whenOwnedByCurrentUser() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(owner);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        subscriptionService.updateUsageStatus(10L, new UsageUpdateRequest(false));

        assertThat(subscription.isUsedRecently()).isFalse();
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void updateUsageStatus_throwsUnauthorized_whenSubscriptionBelongsToAnotherUser() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(otherUser);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> subscriptionService.updateUsageStatus(10L, new UsageUpdateRequest(false)))
                .isInstanceOf(UnauthorizedActionException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void cancelSubscription_setsActiveFalse() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(owner);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        subscriptionService.cancelSubscription(10L);

        assertThat(subscription.isActive()).isFalse();
    }

    @Test
    void getSubscriptionById_throwsResourceNotFound_whenIdDoesNotExist() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(owner);
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteSubscription_removesIt_whenOwnedByCurrentUser() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(owner);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        subscriptionService.deleteSubscription(10L);

        verify(subscriptionRepository).delete(subscription);
    }
}