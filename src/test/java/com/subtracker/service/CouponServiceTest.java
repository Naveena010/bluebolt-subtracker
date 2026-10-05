package com.subtracker.service;

import com.subtracker.dto.request.CouponRequest;
import com.subtracker.dto.request.RedeemCouponRequest;
import com.subtracker.entity.Coupon;
import com.subtracker.entity.Subscription;
import com.subtracker.entity.User;
import com.subtracker.enums.Role;
import com.subtracker.exception.*;
import com.subtracker.repository.CouponRedemptionRepository;
import com.subtracker.repository.CouponRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private CouponRedemptionRepository couponRedemptionRepository;

    @InjectMocks
    private CouponService couponService;

    private User admin;
    private User customer;
    private Coupon activeCoupon;
    private Subscription subscription;
    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).email("admin@subtracker.com").role(Role.ADMIN).build();
        customer = User.builder().id(2L).email("customer@example.com").role(Role.CUSTOMER).build();

        activeCoupon = Coupon.builder()
                .id(100L)
                .code("SAVE20")
                .discountPercentage(20.0)
                .expiryDate(LocalDate.now().plusDays(30))
                .active(true)
                .createdBy(admin)
                .build();

        subscription = Subscription.builder()
                .id(10L)
                .serviceName("Netflix")
                .amount(BigDecimal.valueOf(500))
                .active(true)
                .user(customer)
                .build();

        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    void createCoupon_savesCoupon_whenCodeIsUnique() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(admin);
        when(couponRepository.existsByCode("SAVE20")).thenReturn(false);

        CouponRequest request = new CouponRequest("SAVE20", 20.0, LocalDate.now().plusDays(10));
        couponService.createCoupon(request);

        verify(couponRepository).save(argThat(c -> c.getCode().equals("SAVE20")));
    }

    @Test
    void createCoupon_throwsDuplicateResourceException_whenCodeAlreadyExists() {
        when(couponRepository.existsByCode("SAVE20")).thenReturn(true);

        CouponRequest request = new CouponRequest("SAVE20", 20.0, LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void redeemCoupon_appliesDiscount_whenEverythingValid() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(customer);
        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(activeCoupon));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(couponRedemptionRepository.existsByCouponIdAndSubscriptionId(100L, 10L)).thenReturn(false);

        var response = couponService.redeemCoupon(new RedeemCouponRequest("SAVE20", 10L));

        assertThat(response.originalAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.discountedAmount()).isEqualByComparingTo(BigDecimal.valueOf(400).setScale(2));
        verify(couponRedemptionRepository).save(any());
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void redeemCoupon_throwsCouponExpiredException_whenCouponIsExpired() {
        Coupon expiredCoupon = Coupon.builder()
                .id(101L).code("OLD10").discountPercentage(10.0)
                .expiryDate(LocalDate.now().minusDays(1)).active(true).build();

        when(couponRepository.findByCode("OLD10")).thenReturn(Optional.of(expiredCoupon));

        assertThatThrownBy(() -> couponService.redeemCoupon(new RedeemCouponRequest("OLD10", 10L)))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    void redeemCoupon_throwsUnauthorizedActionException_whenSubscriptionBelongsToAnotherUser() {
        User intruder = User.builder().id(99L).email("intruder@example.com").role(Role.CUSTOMER).build();
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(intruder);
        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(activeCoupon));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> couponService.redeemCoupon(new RedeemCouponRequest("SAVE20", 10L)))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void redeemCoupon_throwsSubscriptionInactiveException_whenSubscriptionIsCancelled() {
        subscription.setActive(false);
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(customer);
        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(activeCoupon));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> couponService.redeemCoupon(new RedeemCouponRequest("SAVE20", 10L)))
                .isInstanceOf(SubscriptionInactiveException.class);
    }

    @Test
    void redeemCoupon_throwsCouponAlreadyRedeemedException_whenAlreadyAppliedToSameSubscription() {
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(customer);
        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(activeCoupon));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(couponRedemptionRepository.existsByCouponIdAndSubscriptionId(100L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> couponService.redeemCoupon(new RedeemCouponRequest("SAVE20", 10L)))
                .isInstanceOf(CouponAlreadyRedeemedException.class);
    }

    @Test
    void redeemCoupon_throwsResourceNotFoundException_whenCouponCodeInvalid() {
        when(couponRepository.findByCode("FAKE99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.redeemCoupon(new RedeemCouponRequest("FAKE99", 10L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}