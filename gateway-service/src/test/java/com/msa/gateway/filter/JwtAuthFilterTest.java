package com.msa.gateway.filter;

import com.msa.gateway.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class JwtAuthFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("인증이 필요한 API에 토큰 없이 요청하면 401")
    void protectedApi_noToken_returns401() {
        webTestClient.get()
                .uri("/users/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("유효한 토큰으로 요청하면 필터를 통과한다 (401이 아님)")
    void protectedApi_validToken_passesFilter() {
        String token = jwtUtil.generateToken(1L);

        webTestClient.get()
                .uri("/users/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().value(status ->
                        assertThat(status).isNotEqualTo(401));
    }

    @Test
    @DisplayName("잘못된 토큰으로 요청하면 401")
    void protectedApi_invalidToken_returns401() {
        webTestClient.get()
                .uri("/users/1")
                .header("Authorization", "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("공개 API(회원가입)는 토큰 없이도 필터를 통과한다")
    void publicApi_signup_noToken_passes() {
        webTestClient.post()
                .uri("/users")
                .exchange()
                .expectStatus().value(status ->
                        assertThat(status).isNotEqualTo(401));
    }

    @Test
    @DisplayName("공개 API(로그인)는 토큰 없이도 필터를 통과한다")
    void loginApi_noToken_passes() {
        webTestClient.post()
                .uri("/users/login")
                .exchange()
                .expectStatus().value(status ->
                        assertThat(status).isNotEqualTo(401));
    }

    @Test
    @DisplayName("공개 API(게시글 목록)는 토큰 없이도 필터를 통과한다")
    void boardListApi_noToken_passes() {
        webTestClient.get()
                .uri("/boards")
                .exchange()
                .expectStatus().value(status ->
                        assertThat(status).isNotEqualTo(401));
    }
}
