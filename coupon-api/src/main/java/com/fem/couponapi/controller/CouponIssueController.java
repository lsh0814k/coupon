package com.fem.couponapi.controller;

import com.fem.couponapi.controller.dto.CouponIssueRequestDto;
import com.fem.couponapi.controller.dto.CouponIssueResponseDto;
import com.fem.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {
    private final CouponIssueRequestService couponIssueRequestService;

    /**
     * 동시성 문제를 자바의 synchronized 블록을 이용하여 처리
     * api server 가 분산환경이면 동시성 문제가 다시 발생할 수 있다.
     */
    @PostMapping("/v1/issue")
    public CouponIssueResponseDto issueV1(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.issueRequestV1(body);
        return new CouponIssueResponseDto(true, null);
    }

    /**
     * 동시성 문제를 redisson 을 이용하여 처리
     * api server 가 분산환경 이더라도 동시성 문제가 발생하지 않는다.
     * synchronized 블록 보다 rps 가 낮다.
     *
     */
    @PostMapping("/v2/issue")
    public CouponIssueResponseDto issueV2(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.issueRequestV2(body);
        return new CouponIssueResponseDto(true, null);
    }

    @PostMapping("/v3/issue")
    public CouponIssueResponseDto issueV3(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.issueRequestV3(body);
        return new CouponIssueResponseDto(true, null);
    }

    @PostMapping("/v1/issue-async")
    public CouponIssueResponseDto asyncIssueV1(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.asyncIssueRequestV1(body);
        return new CouponIssueResponseDto(true, null);
    }
}
