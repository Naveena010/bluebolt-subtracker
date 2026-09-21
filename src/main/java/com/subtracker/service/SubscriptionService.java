package com.subtracker.service;

import com.subtracker.dto.request.SubscriptionRequest;
import com.subtracker.dto.request.UsageUpdateRequest;
import com.subtracker.dto.response.SubscriptionResponse;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;
import com.subtracker.exception.ResourceNotFoundException;
import com.subtracker.exception.UnauthorizedActionException;
import com.subtracker.mapper.SubscriptionMapper;
import com.subtracker.repository.SubscriptionRepository;
import com.subtracker.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionResponse addSubscription(SubscriptionRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();
        Subscription subscription = SubscriptionMapper.toEntity(request, currentUser);
        subscriptionRepository.save(subscription);
        return SubscriptionMapper.toResponse(subscription);
    }

    public List<SubscriptionResponse> getMySubscriptions() {
        User currentUser = SecurityUtil.getCurrentUser();
        return subscriptionRepository.findByUserId(currentUser.getId())
                .stream()
                .map(SubscriptionMapper::toResponse)
                .toList();
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription subscription = findAndVerifyOwnership(id);
        return SubscriptionMapper.toResponse(subscription);
    }

    public SubscriptionResponse updateUsageStatus(Long id, UsageUpdateRequest request) {
        Subscription subscription = findAndVerifyOwnership(id);
        subscription.setUsedRecently(request.usedRecently());
        subscriptionRepository.save(subscription);
        return SubscriptionMapper.toResponse(subscription);
    }

    public SubscriptionResponse cancelSubscription(Long id) {
        Subscription subscription = findAndVerifyOwnership(id);
        subscription.setActive(false);
        subscriptionRepository.save(subscription);
        return SubscriptionMapper.toResponse(subscription);
    }

    public void deleteSubscription(Long id) {
        Subscription subscription = findAndVerifyOwnership(id);
        subscriptionRepository.delete(subscription);
    }

    // --- Internal helper: fetch subscription and ensure it belongs to the logged-in user ---
    private Subscription findAndVerifyOwnership(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        User currentUser = SecurityUtil.getCurrentUser();
        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not allowed to access this subscription");
        }
        return subscription;
    }
}