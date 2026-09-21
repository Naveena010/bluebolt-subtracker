package com.subtracker.service;

import com.subtracker.dto.request.CouponRequest;
import com.subtracker.dto.request.RedeemCouponRequest;
import com.subtracker.dto.response.CouponResponse;
import com.subtracker.dto.response.RedeemCouponResponse;
import com.subtracker.entity.Coupon;
import com.subtracker.entity.CouponRedemption;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;
import com.subtracker.exception.*;
import com.subtracker.mapper.CouponMapper;
import com.subtracker.repository.CouponRedemptionRepository;
import com.subtracker.repository.CouponRepository;
import com.subtracker.repository.SubscriptionRepository;
import com.subtracker.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;

    // ---------- ADMIN ACTIONS ----------

    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Coupon code already exists: " + request.code());
        }

        User admin = SecurityUtil.getCurrentUser();
        Coupon coupon = CouponMapper.toEntity(request, admin);
        couponRepository.save(coupon);
        return CouponMapper.toResponse(coupon);
    }

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(CouponMapper::toResponse)
                .toList();
    }

    public CouponResponse deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        coupon.setActive(false);
        couponRepository.save(coupon);
        return CouponMapper.toResponse(coupon);
    }

    // ---------- CUSTOMER ACTIONS ----------

    public RedeemCouponResponse redeemCoupon(RedeemCouponRequest request) {
        Coupon coupon = couponRepository.findByCode(request.couponCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code: " + request.couponCode()));

        if (!coupon.isActive()) {
            throw new CouponExpiredException("This coupon is no longer active");
        }

        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CouponExpiredException("This coupon expired on " + coupon.getExpiryDate());
        }

        User currentUser = SecurityUtil.getCurrentUser();

        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + request.subscriptionId()));

        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not allowed to apply a coupon to this subscription");
        }

        if (!subscription.isActive()) {
            throw new SubscriptionInactiveException("Cannot apply coupon to a cancelled subscription");
        }

        boolean alreadyRedeemed = couponRedemptionRepository
                .existsByCouponIdAndSubscriptionId(coupon.getId(), subscription.getId());
        if (alreadyRedeemed) {
            throw new CouponAlreadyRedeemedException("This coupon has already been applied to this subscription");
        }

        BigDecimal originalAmount = subscription.getAmount();
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - (coupon.getDiscountPercentage() / 100));
        BigDecimal discountedAmount = originalAmount.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);

        // Persist redemption record so this coupon can't be reused on the same subscription
        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .user(currentUser)
                .subscription(subscription)
                .build();
        couponRedemptionRepository.save(redemption);

        // Apply new discounted amount to the subscription going forward
        subscription.setAmount(discountedAmount);
        subscriptionRepository.save(subscription);

        return new RedeemCouponResponse(
                coupon.getCode(),
                coupon.getDiscountPercentage(),
                originalAmount,
                discountedAmount,
                "Coupon applied successfully. Your next billing amount is updated."
        );
    }
}