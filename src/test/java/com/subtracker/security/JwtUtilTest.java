package com.subtracker.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "ThisIsA256BitSecretKeyForJWTSigningChangeThisInProduction123");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_andExtractEmail_roundTripsCorrectly() {
        String token = jwtUtil.generateToken("jordan@example.com", "CUSTOMER");

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("jordan@example.com");
    }

    @Test
    void generateToken_andExtractRole_roundTripsCorrectly() {
        String token = jwtUtil.generateToken("admin@subtracker.com", "ADMIN");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_returnsTrue_forFreshlyGeneratedToken() {
        String token = jwtUtil.generateToken("jordan@example.com", "CUSTOMER");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forGarbageToken() {
        assertThat(jwtUtil.isTokenValid("not-a-real-token")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_whenSignedWithDifferentSecret() {
        String token = jwtUtil.generateToken("jordan@example.com", "CUSTOMER");

        JwtUtil differentUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentUtil, "secret", "ADifferentSecretKeyThatWontMatchAtAll000000");
        ReflectionTestUtils.setField(differentUtil, "expiration", 86400000L);

        assertThat(differentUtil.isTokenValid(token)).isFalse();
    }
}