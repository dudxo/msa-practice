package com.msa.user.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("msa-secret-key-for-jwt-token-must-be-long-enough-256bit", 3600000);
    }

    @Test
    @DisplayName("토큰 생성 후 userId를 추출할 수 있다")
    void generateAndExtractUserId() {
        String token = jwtUtil.generateToken(1L);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("유효한 토큰은 검증에 성공한다")
    void validateToken_valid() {
        String token = jwtUtil.generateToken(1L);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰은 검증에 실패한다")
    void validateToken_invalid() {
        assertThat(jwtUtil.validateToken("invalid-token")).isFalse();
    }
}
