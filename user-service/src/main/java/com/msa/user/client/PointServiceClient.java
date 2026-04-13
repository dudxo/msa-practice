package com.msa.user.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointServiceClient {

    private final RestTemplate restTemplate;

    @Value("${point-service.url:http://point-service}")
    private String pointServiceUrl;

    @CircuitBreaker(name = "pointService", fallbackMethod = "earnPointsFallback")
    public void earnPoints(Long userId, int amount, String description) {
        String url = pointServiceUrl + "/points/earn";
        restTemplate.postForObject(url, Map.of(
                "userId", userId,
                "amount", amount,
                "description", description
        ), Object.class);
    }

    private void earnPointsFallback(Long userId, int amount, String description, Throwable t) {
        log.warn("Point Service 적립 호출 실패 (userId={}, amount={}): {}", userId, amount, t.getMessage());
    }
}
