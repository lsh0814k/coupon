package com.fem.couponapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-core")
class CouponApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
