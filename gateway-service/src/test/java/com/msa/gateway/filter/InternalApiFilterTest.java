package com.msa.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class InternalApiFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("/internal/** 경로는 외부에서 접근 시 403 Forbidden")
    void internalApi_blockedFromExternal() {
        webTestClient.post()
                .uri("/internal/users/by-ids")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("/internal/points 경로도 외부에서 차단된다")
    void internalPointsApi_blockedFromExternal() {
        webTestClient.post()
                .uri("/internal/anything")
                .exchange()
                .expectStatus().isForbidden();
    }
}
