package com.subtracker.mapper;

import com.subtracker.dto.request.SubscriptionRequest;
import com.subtracker.dto.response.SubscriptionResponse;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;

public class SubscriptionMapper {

    public static Subscription toEntity(SubscriptionRequest request, User user) {
        return Subscription.builder()
                .serviceName(request.serviceName())
                .amount(request.amount())
                .billingCycle(request.billingCycle())
                .nextBillingDate(request.nextBillingDate())
                .active(true)
                .usedRecently(true)
                .user(user)
                .build();
    }

    public static SubscriptionResponse toResponse(Subscription sub) {
        return new SubscriptionResponse(
                sub.getId(),
                sub.getServiceName(),
                sub.getAmount(),
                sub.getBillingCycle(),
                sub.getNextBillingDate(),
                sub.isActive(),
                sub.isUsedRecently()
        );
    }
}