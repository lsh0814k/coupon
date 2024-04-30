package com.fem.couponcore.repository.mysql;

import com.fem.couponcore.model.CouponIssue;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.fem.couponcore.model.QCouponIssue.couponIssue;

@Repository
@RequiredArgsConstructor
public class CouponIssueRepository {
    private final JPAQueryFactory queryFactory;


    public Optional<CouponIssue> findFirstCouponIssue(Long couponId, Long userId) {
        CouponIssue issue = queryFactory.selectFrom(couponIssue)
                .where(couponIssue.couponId.eq(couponId))
                .where(couponIssue.userId.eq(userId))
                .fetchFirst();
        if (issue == null) {
            return Optional.empty();
        }

        return Optional.of(issue);
    }
}
