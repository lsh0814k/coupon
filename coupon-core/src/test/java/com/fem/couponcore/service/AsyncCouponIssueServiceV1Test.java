package com.fem.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fem.couponcore.TestConfig;
import com.fem.couponcore.exception.CouponIssueException;
import com.fem.couponcore.model.Coupon;
import com.fem.couponcore.model.CouponType;
import com.fem.couponcore.repository.mysql.CouponJpaRepository;
import com.fem.couponcore.repository.redis.dto.CouponIssueRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static com.fem.couponcore.exception.ErrorCode.*;
import static com.fem.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.fem.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;
import static org.junit.jupiter.api.Assertions.*;

class AsyncCouponIssueServiceV1Test extends TestConfig {

    @Autowired AsyncCouponIssueServiceV1 sut;
    @Autowired RedisTemplate<String, String> redisTemplate;
    @Autowired CouponJpaRepository couponJpaRepository;


    @BeforeEach
    void clear() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("쿠폰 발급 - 쿠폰이 존재하지 않는다면 예외를 반환한다.")
    void issue_1() {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        // when, then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> sut.issue(couponId, userId));
        assertEquals(COUPON_NOT_EXIST, ex.getErrorCode());
    }

    @Test
    @DisplayName("쿠폰 발급 - 발급 가능 수량이 존재하지 않는다면 예외를 반환한다.")
    void issue_2() {
        // given
        Long userId = 1000L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        IntStream.range(0, coupon.getTotalQuantity()).forEach(idx ->
            redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(idx))
        );
        // when, then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        assertEquals(INVALID_COUPON_ISSUE_QUANTITY, ex.getErrorCode());
    }

    @Test
    @DisplayName("쿠폰 발급 - 이미 발급된 유저라면 예외를 반환한다.")
    void issue_3() {
        // given
        Long userId = 1000L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        // when, then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        assertEquals(DUPLICATED_COUPON_ISSUE, ex.getErrorCode());
    }

    @Test
    @DisplayName("쿠폰 발급 - 발급 기간이 유효하지 않다면 예외를 반환한다.")
    void issue_4() {
        // given
        Long userId = 1000L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // when, then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        assertEquals(INVALID_COUPON_ISSUE_DATE, ex.getErrorCode());
    }

    @Test
    @DisplayName("쿠폰 발급 - 쿠폰 발급을 기록한다.")
    void issue_5() {
        // given
        Long userId = 1000L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // when
        sut.issue(coupon.getId(), userId);

        // then
        Boolean result = redisTemplate.opsForSet().isMember(getIssueRequestKey(coupon.getId()), userId.toString());
        assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 발급 - 쿠폰 발급 요청이 성공하면 쿠폰 발급 큐에 적재된다.")
    void issue_6() throws JsonProcessingException {
        // given
        Long userId = 1000L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        ObjectMapper objectMapper = new ObjectMapper();
        String request = objectMapper.writeValueAsString(new CouponIssueRequest(coupon.getId(), userId));

        // when
        sut.issue(coupon.getId(), userId);

        // then
        String result = redisTemplate.opsForList().leftPop(getIssueRequestQueueKey(coupon.getId()));
        assertEquals(request, result);
    }
}