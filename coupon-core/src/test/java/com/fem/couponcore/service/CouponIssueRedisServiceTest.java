package com.fem.couponcore.service;

import com.fem.couponcore.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.stream.IntStream;

import static com.fem.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.junit.jupiter.api.Assertions.*;

class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired private CouponIssueRedisService sut;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clear() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }


    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true 를 반환한다.")
    void availableTotalIssueQuantity_1() {
        // given
        int totalIssueQuantity = 10;
        Long couponId = 1L;

        // when
        boolean result = sut.availableTotalIssueQuantity(couponId, totalIssueQuantity);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 모두 소진된 경우 false 를 반환한다.")
    void availableTotalIssueQuantity_2() {
        // given
        int totalIssueQuantity = 10;
        Long couponId = 1L;
        IntStream.range(0, totalIssueQuantity).forEach(userId ->
                redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId)));

        // when
        boolean result = sut.availableTotalIssueQuantity(couponId, totalIssueQuantity);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하지 않으면 true를 반환한다.")
    void availableUserIssueQuantity_1() {
        // given
        Long couponId = 1L;
        Long userId = 1L;

        // when
        boolean result = sut.availableUserIssueQuantity(couponId, userId);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하면 false를 반환한다.")
    void availableUserIssueQuantity_2() {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), userId.toString());

        // when
        boolean result = sut.availableUserIssueQuantity(couponId, userId);

        // then
        assertFalse(result);
    }
}