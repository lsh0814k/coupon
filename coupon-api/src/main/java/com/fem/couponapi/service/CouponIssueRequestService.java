package com.fem.couponapi.service;

import com.fem.couponapi.controller.dto.CouponIssueRequestDto;
import com.fem.couponcore.component.DistributeLockExecutor;
import com.fem.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        synchronized (this) {
            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        }
        log.info("쿠폰 발급 완료. couponId : {}, userId: {}", requestDto.couponId(), requestDto.userId());
    }

    public void issueRequestV2(CouponIssueRequestDto requestDto) {
        distributeLockExecutor.execute("lock_" + requestDto.couponId(), 10000, 10000,
                () -> couponIssueService.issue(requestDto.couponId(), requestDto.userId()));

        log.info("쿠폰 발급 완료. couponId : {}, userId: {}", requestDto.couponId(), requestDto.userId());
    }
}
