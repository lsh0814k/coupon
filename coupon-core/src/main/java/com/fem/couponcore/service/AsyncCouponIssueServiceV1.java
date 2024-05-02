package com.fem.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fem.couponcore.component.DistributeLockExecutor;
import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.model.Coupon;
import com.fem.couponcore.repository.redis.CouponRedisRepository;
import com.fem.couponcore.repository.redis.dto.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.fem.couponcore.exception.ErrorCode.*;
import static com.fem.couponcore.util.CouponRedisUtils.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final CouponRedisRepository couponRedisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(Long couponId, Long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        if (!coupon.availableIssuedDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. couponId : %s, userId : %s"
                    .formatted(couponId, userId));
        }

        distributeLockExecutor.execute(getLockKey(couponId), 5000, 5000, () -> {
            if (!couponIssueRedisService.availableTotalIssueQuantity(couponId, coupon.getTotalQuantity())) {
                throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponId : %s, userId : %s"
                        .formatted(couponId, userId));
            }

            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리됐습니다. couponId : %s, userId : %s"
                        .formatted(couponId, userId));
            }

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
