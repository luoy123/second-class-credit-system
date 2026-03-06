package com.secondclass.credit.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void generateAndExtractRoleShouldWork() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "secondClassCreditJwtSecretKeyForHmacSha256_2026");
        ReflectionTestUtils.setField(jwtService, "expireSeconds", 7200L);

        String token = jwtService.generateToken("admin", "ADMIN");
        String role = jwtService.extractRole(token);

        assertEquals("ADMIN", role);
    }
}
