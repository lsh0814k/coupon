package com.fem.couponapi.controller;

import com.fem.couponapi.controller.dto.CouponIssueResponseDto;
import com.fem.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponControllerAdvice {

    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponIssueException e) {
        return new CouponIssueResponseDto(false, e.getErrorCode().message);
    }
}
