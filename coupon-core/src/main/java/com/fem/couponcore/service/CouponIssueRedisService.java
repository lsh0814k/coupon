package com.fem.couponcore.service;

import com.fem.couponcore.repository.redis.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.fem.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {
    private final CouponRedisRepository couponRedisRepository;

    public boolean availableTotalIssueQuantity(Long couponId, Integer totalQuantity) {
        if (totalQuantity == null) {
            return true;
        }

        String key = getIssueRequestKey(couponId);
        return totalQuantity > couponRedisRepository.sCard(key);
    }

    public boolean availableUserIssueQuantity(Long couponId, Long userId) {
        String key = getIssueRequestKey(couponId);
        return !couponRedisRepository.sIsMember(key, userId.toString());
    }
}
