package com.subtracker.exception;

public class CouponAlreadyRedeemedException extends RuntimeException {
    public CouponAlreadyRedeemedException(String message) {
        super(message);
    }
}