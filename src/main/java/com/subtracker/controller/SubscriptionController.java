package com.subtracker.controller;

import com.subtracker.dto.request.SubscriptionRequest;
import com.subtracker.dto.request.UsageUpdateRequest;
import com.subtracker.dto.response.PageResponse;
import com.subtracker.dto.response.SubscriptionResponse;
import com.subtracker.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> addSubscription(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.addSubscription(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SubscriptionResponse>> getMySubscriptions(
            @PageableDefault(size = 10, sort = "nextBillingDate") Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @PatchMapping("/{id}/usage")
    public ResponseEntity<SubscriptionResponse> updateUsage(@PathVariable Long id,
                                                            @Valid @RequestBody UsageUpdateRequest request) {
        return ResponseEntity.ok(subscriptionService.updateUsageStatus(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok("Subscription deleted successfully");
    }
}