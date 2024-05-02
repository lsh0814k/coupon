package com.fem.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fem.couponcore.component.DistributeLockExecutor;
import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.repository.redis.CouponRedisRepository;
import com.fem.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fem.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.fem.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.fem.couponcore.util.CouponRedisUtils.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final CouponRedisRepository couponRedisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponCacheService couponCacheService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(Long couponId, Long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();

        distributeLockExecutor.execute(getLockKey(couponId), 5000, 5000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(Long couponId, Long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(issueRequest);
            couponRedisRepository.sAdd(getIssueRequestKey(couponId), userId.toString());
            couponRedisRepository.rPush(getIssueRequestQueueKey(couponId), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }
}
