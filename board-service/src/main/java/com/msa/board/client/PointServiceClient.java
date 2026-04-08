package com.msa.board.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointServiceClient {

    private final RestTemplate restTemplate;

    @Value("${point-service.url:http://localhost:8083}")
    private String pointServiceUrl;

    public void deductPoints(Long userId, int amount, String description) {
        String url = pointServiceUrl + "/points/deduct";
        try {
            restTemplate.postForObject(url, Map.of(
                    "userId", userId,
                    "amount", amount,
                    "description", description
            ), Object.class);
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
    }

    @CircuitBreaker(name = "pointService", fallbackMethod = "earnPointsFallback")
    public void earnPoints(Long userId, int amount, String description) {
        String url = pointServiceUrl + "/points/earn";
        restTemplate.postForObject(url, Map.of(
                "userId", userId,
                "amount", amount,
                "description", description
        ), Object.class);
    }

    public void refundPoints(Long userId, int amount, String description) {
        String url = pointServiceUrl + "/points/refund";
        try {
            restTemplate.postForObject(url, Map.of(
                    "userId", userId,
                    "amount", amount,
                    "description", description
            ), Object.class);
            log.info("포인트 환불 성공 (userId={}, amount={})", userId, amount);
        } catch (Exception e) {
            log.error("포인트 환불 실패! 수동 확인 필요 (userId={}, amount={}): {}", userId, amount, e.getMessage());
        }
    }

    private void earnPointsFallback(Long userId, int amount, String description, Throwable t) {
        log.warn("Point Service 적립 호출 실패 (userId={}, amount={}): {}", userId, amount, t.getMessage());
    }
}
