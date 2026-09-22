package com.subtracker.service;

import com.subtracker.dto.response.DashboardResponse;
import com.subtracker.dto.response.ServiceCancelCount;
import com.subtracker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SubscriptionRepository subscriptionRepository;

    public DashboardResponse getDashboard() {
        var totalMoneySaved = subscriptionRepository.getTotalMoneySaved();
        var activeUsersCount = subscriptionRepository.countActiveUsers();
        var totalSubscriptionsTracked = subscriptionRepository.count();
        var totalCancelledSubscriptions = subscriptionRepository.countByActiveFalse();

        List<ServiceCancelCount> mostCancelled =
                subscriptionRepository.findMostCancelledServices(PageRequest.of(0, 5));

        return new DashboardResponse(
                totalMoneySaved,
                activeUsersCount,
                totalSubscriptionsTracked,
                totalCancelledSubscriptions,
                mostCancelled
        );
    }
}