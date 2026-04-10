package com.msa.gateway.filter;

import com.msa.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter, Ordered {

    private final JwtUtil jwtUtil;

    // 인증 없이 접근 가능한 경로
    private static final List<String> PUBLIC_PATHS = List.of(
            "POST:/users",        // 회원가입
            "POST:/users/login",  // 로그인
            "GET:/boards",        // 게시글 목록
            "GET:/boards/"        // 게시글 상세 (prefix 매칭)
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // 공개 API는 인증 불필요
        if (isPublicPath(method, path)) {
            return chain.filter(exchange);
        }

        // Authorization 헤더 확인
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 토큰에서 userId를 꺼내서 헤더에 추가 → 다운스트림 서비스로 전달
        // 기존에 클라이언트가 보낸 X-User-Id는 제거 (위조 방지)
        Long userId = jwtUtil.extractUserId(token);

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .headers(h -> h.remove("X-User-Id"))
                .header("X-User-Id", String.valueOf(userId))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String method, String path) {
        String key = method + ":" + path;
        return PUBLIC_PATHS.stream().anyMatch(p -> {
            if (p.endsWith("/")) {
                // prefix 매칭 (GET:/boards/ → GET:/boards/1, GET:/boards/123 등)
                return key.startsWith(p);
            }
            return key.equals(p);
        });
    }

    @Override
    public int getOrder() {
        return 0; // InternalApiFilter(-1) 다음에 실행
    }
}
