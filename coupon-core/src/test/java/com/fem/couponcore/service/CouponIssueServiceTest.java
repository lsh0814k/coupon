package com.fem.couponcore.service;

import com.fem.couponcore.TestConfig;
import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.exception.ErrorCode;
import com.fem.couponcore.model.Coupon;
import com.fem.couponcore.model.CouponIssue;
import com.fem.couponcore.model.CouponType;
import com.fem.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.fem.couponcore.repository.mysql.CouponIssueRepository;
import com.fem.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.fem.couponcore.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

class CouponIssueServiceTest extends TestConfig {

    @Autowired private CouponIssueService sut;
    @Autowired private CouponJpaRepository couponJpaRepository;
    @Autowired private CouponIssueJpaRepository couponIssueJpaRepository;
    @Autowired private CouponIssueRepository couponIssueRepository;

    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외를 반환한다.")
    void saveCouponIssue_1() {
        // given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        couponIssueJpaRepository.save(couponIssue);
        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> sut.saveCouponIssue(1L, 1L));
        assertEquals(couponIssueException.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하지 않는다면 쿠폰을 발급한다.")
    void saveCouponIssue_2() {
        // given
        Long couponId = 1L;
        Long userId = 1L;

        // when
        CouponIssue result = sut.saveCouponIssue(couponId, userId);

        // then
        assertTrue(couponIssueJpaRepository.findById(result.getId()).isPresent());
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠픈올 발급한다.")
    void issue_1() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssuedEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // when
        sut.issue(coupon.getId(), userId);

        // then
        Coupon couponResult = couponJpaRepository.findById(coupon.getId()).get();
        assertEquals(couponResult.getIssuedQuantity(), 1);

        Optional<CouponIssue> issue = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        assertTrue(issue.isPresent());
    }

    @Test
    @DisplayName("발급 수량에 문제가 있다면 예외를 반환한다.")
    void issue_2() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssuedEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        assertEquals(couponIssueException.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 기한에 문제가 있다면 예외를 반환한다.")
    void issue_3() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssuedEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        assertEquals(couponIssueException.getErrorCode(), INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("중복 발급 검증에 문제가 있다면 예외를 반환한다.")
    void issue_4() {
        // given
        Long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssuedEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        assertEquals(couponIssueException.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않는다면 예외를 반환한다.")
    void issue_5() {
        // given
        Long userId = 1L;
        Long couponId = 1L;

        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> sut.issue(couponId, userId));
        assertEquals(couponIssueException.getErrorCode(), COUPON_NOT_EXIST);
    }
}