package com.subtracker.mapper;

import com.subtracker.dto.request.SubscriptionRequest;
import com.subtracker.dto.response.SubscriptionResponse;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionMapperTest {

    private final User owner = User.builder()
            .id(1L)
            .fullName("Jordan Rivera")
            .email("jordan@example.com")
            .role(Role.CUSTOMER)
            .build();

    @Test
    void toEntity_mapsRequestFieldsAndAttachesUser() {
        SubscriptionRequest request = new SubscriptionRequest(
                "Netflix", BigDecimal.valueOf(499), "MONTHLY", LocalDate.now().plusDays(5));

        Subscription subscription = SubscriptionMapper.toEntity(request, owner);

        assertThat(subscription.getServiceName()).isEqualTo("Netflix");
        assertThat(subscription.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(499));
        assertThat(subscription.getBillingCycle()).isEqualTo("MONTHLY");
        assertThat(subscription.getUser()).isEqualTo(owner);
    }

    @Test
    void toEntity_defaultsActiveAndUsedRecentlyToTrue() {
        SubscriptionRequest request = new SubscriptionRequest(
                "Spotify", BigDecimal.valueOf(199), "MONTHLY", LocalDate.now().plusDays(10));

        Subscription subscription = SubscriptionMapper.toEntity(request, owner);

        assertThat(subscription.isActive()).isTrue();
        assertThat(subscription.isUsedRecently()).isTrue();
    }

    @Test
    void toResponse_mapsAllEntityFieldsCorrectly() {
        LocalDate billingDate = LocalDate.now().plusDays(7);

        Subscription subscription = Subscription.builder()
                .id(10L)
                .serviceName("Netflix")
                .amount(BigDecimal.valueOf(499))
                .billingCycle("MONTHLY")
                .nextBillingDate(billingDate)
                .active(true)
                .usedRecently(false)
                .user(owner)
                .build();

        SubscriptionResponse response = SubscriptionMapper.toResponse(subscription);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.serviceName()).isEqualTo("Netflix");
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(499));
        assertThat(response.billingCycle()).isEqualTo("MONTHLY");
        assertThat(response.nextBillingDate()).isEqualTo(billingDate);
        assertThat(response.active()).isTrue();
        assertThat(response.usedRecently()).isFalse();
    }

    @Test
    void toResponse_reflectsCancelledState() {
        Subscription cancelled = Subscription.builder()
                .id(11L)
                .serviceName("Hulu")
                .amount(BigDecimal.valueOf(299))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.now().plusDays(3))
                .active(false)
                .usedRecently(true)
                .user(owner)
                .build();

        SubscriptionResponse response = SubscriptionMapper.toResponse(cancelled);

        assertThat(response.active()).isFalse();
    }
}