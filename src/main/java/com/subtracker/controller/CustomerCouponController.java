package com.subtracker.controller;

import com.subtracker.dto.request.RedeemCouponRequest;
import com.subtracker.dto.response.RedeemCouponResponse;
import com.subtracker.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/coupons")
@RequiredArgsConstructor
public class CustomerCouponController {

    private final CouponService couponService;

    @PostMapping("/redeem")
    public ResponseEntity<RedeemCouponResponse> redeemCoupon(@Valid @RequestBody RedeemCouponRequest request) {
        return ResponseEntity.ok(couponService.redeemCoupon(request));
    }
}