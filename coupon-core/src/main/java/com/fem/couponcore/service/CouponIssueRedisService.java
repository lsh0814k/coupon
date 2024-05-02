package com.fem.couponcore.service;

import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.repository.redis.CouponRedisRepository;
import com.fem.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.fem.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.fem.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static com.fem.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {
    private final CouponRedisRepository couponRedisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity couponRedisEntity, Long userId) {
        if (!availableTotalIssueQuantity(couponRedisEntity.id(), couponRedisEntity.totalQuantity())) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponId : %s, userId : %s"
                    .formatted(couponRedisEntity.id(), userId));
        }

        if (!availableUserIssueQuantity(couponRedisEntity.id(), userId)) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리됐습니다. couponId : %s, userId : %s"
                    .formatted(couponRedisEntity.id(), userId));
        }
    }
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
