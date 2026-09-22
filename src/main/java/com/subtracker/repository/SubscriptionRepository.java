package com.subtracker.repository;

import com.subtracker.dto.response.ServiceCancelCount;
import com.subtracker.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Page<Subscription> findByUserId(Long userId, Pageable pageable);

    List<Subscription> findByActiveTrueAndUsedRecentlyFalseAndNextBillingDate(LocalDate date);

    long countByActiveFalse();

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subscription s WHERE s.active = false")
    BigDecimal getTotalMoneySaved();

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Subscription s WHERE s.active = true")
    long countActiveUsers();

    @Query("SELECT new com.subtracker.dto.response.ServiceCancelCount(s.serviceName, COUNT(s)) " +
            "FROM Subscription s WHERE s.active = false " +
            "GROUP BY s.serviceName ORDER BY COUNT(s) DESC")
    List<ServiceCancelCount> findMostCancelledServices(Pageable pageable);
}