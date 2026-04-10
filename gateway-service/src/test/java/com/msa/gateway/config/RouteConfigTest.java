package com.msa.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RouteConfigTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("User Service 라우트가 등록되어 있다")
    void userServiceRouteExists() {
        boolean exists = routeLocator.getRoutes()
                .any(route -> route.getId().equals("user-service"))
                .block();
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Board Service 라우트가 등록되어 있다")
    void boardServiceRouteExists() {
        boolean exists = routeLocator.getRoutes()
                .any(route -> route.getId().equals("board-service"))
                .block();
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Point Service 라우트가 등록되어 있다")
    void pointServiceRouteExists() {
        boolean exists = routeLocator.getRoutes()
                .any(route -> route.getId().equals("point-service"))
                .block();
        assertThat(exists).isTrue();
    }
}
