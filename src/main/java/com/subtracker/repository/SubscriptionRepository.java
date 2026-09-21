package com.subtracker.repository;

import com.subtracker.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByActiveTrueAndUsedRecentlyFalseAndNextBillingDate(LocalDate date);
}