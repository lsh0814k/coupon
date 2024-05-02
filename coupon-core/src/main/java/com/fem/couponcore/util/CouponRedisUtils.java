package com.fem.couponcore.util;

public class CouponRedisUtils {
    public static String getIssueRequestKey(Long couponId) {
        return "issue:request:couponId:%s".formatted(couponId);
    }

    public static String getIssueRequestQueueKey(Long couponId) {
        return "issue:request:queue:couponId:%s".formatted(couponId);
    }

    public static String getLockKey(Long couponId) {
        return "lock:%s".formatted(couponId);
    }
}
