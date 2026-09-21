package com.subtracker.repository;

import com.subtracker.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {
    boolean existsByCouponIdAndSubscriptionId(Long couponId, Long subscriptionId);
    List<CouponRedemption> findByUserId(Long userId);
}