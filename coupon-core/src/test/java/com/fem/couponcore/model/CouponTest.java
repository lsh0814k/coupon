package com.fem.couponcore.model;


import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    @DisplayName("발급 수량이 남아 있다면 true 를 반환한다.")
    void availableIssueQuantity_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();
        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("발급 수량이 소진 되었다면 true 를 반환한다.")
    void availableIssueQuantity_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();
        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("최대 발급 수량이 설정되지 않았다면 true를 반환한다.")
    void availableIssueQuantity_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();
        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 시작되지 않았다면 false 를 반환한다.")
    void availableIssueDate_1() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when
        boolean result = coupon.availableIssuedDate();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("발급 기간에 해당되면 true 를 반환한다.")
    void availableIssueDate_2() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        // when
        boolean result = coupon.availableIssuedDate();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 종료 되었다면 false 를 반환한다.")
    void availableIssueDate_3() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        // when
        boolean result = coupon.availableIssuedDate();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("발급 수량과 발급 기간이 유효하다면 발급에 성공한다.")
    void issue_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when
        coupon.issue();

        // then
        assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @Test
    @DisplayName("발급 수량을 초과하면 예외를 반환한다.")
    void issue_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, coupon::issue);
        assertEquals(couponIssueException.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 수량과 발급 기간이 유효하다면 발급에 성공한다.")
    void issue_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when, then
        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, coupon::issue);
        assertEquals(couponIssueException.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }
}