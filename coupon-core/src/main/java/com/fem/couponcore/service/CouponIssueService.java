package com.fem.couponcore.service;

import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.exception.ErrorCode;
import com.fem.couponcore.model.Coupon;
import com.fem.couponcore.model.CouponIssue;
import com.fem.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.fem.couponcore.repository.mysql.CouponIssueRepository;
import com.fem.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.fem.couponcore.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CouponIssueService {
    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;

    @Transactional
    public void issue(Long couponId, Long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }
    
    @Transactional
    public void issueWithLock(Long couponId, Long userId) {
        Coupon coupon = findCouponWithLock(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }

    @Transactional
    public Coupon findCouponWithLock(Long couponId) {
        return couponJpaRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }

    @Transactional
    public CouponIssue saveCouponIssue(Long couponId, Long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue issue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();

        return couponIssueJpaRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(Long couponId) {
        return couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }

    private void checkAlreadyIssuance(Long couponId, Long userId) {
        Optional<CouponIssue> couponIssue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (couponIssue.isPresent()) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. userId: %s, couponId: %s".formatted(userId, couponId));
        }
    }
}
