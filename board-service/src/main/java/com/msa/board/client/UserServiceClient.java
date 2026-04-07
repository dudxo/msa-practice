package com.msa.board.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserNameFallback")
    public String getUserName(Long userId) {
        String url = userServiceUrl + "/users/" + userId;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        Map<String, Object> body = response.getBody();
        return body != null ? (String) body.get("name") : "알 수 없음";
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserNamesByIdsFallback")
    public Map<Long, String> getUserNamesByIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String url = userServiceUrl + "/internal/users/by-ids";
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(userIds),
                new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> users = response.getBody();
        if (users == null) {
            return Collections.emptyMap();
        }

        return users.stream()
                .collect(Collectors.toMap(
                        u -> ((Number) u.get("id")).longValue(),
                        u -> (String) u.get("name")
                ));
    }

    // Fallback: 단건 조회 실패 시
    private String getUserNameFallback(Long userId, Throwable t) {
        log.warn("User Service 호출 실패 (userId={}): {}", userId, t.getMessage());
        return "알 수 없음";
    }

    // Fallback: 다건 조회 실패 시
    private Map<Long, String> getUserNamesByIdsFallback(List<Long> userIds, Throwable t) {
        log.warn("User Service 다건 조회 실패: {}", t.getMessage());
        return Collections.emptyMap();
    }
}
