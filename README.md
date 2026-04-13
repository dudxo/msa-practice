# MSA 학습 프로젝트

> Java/Spring Boot 기반 Microservices Architecture 프로젝트.
> **분산 시스템의 핵심 문제들을 실제로 구현하며 체득**하는 것을 목표로 하였습니다.

---

## 왜 이 프로젝트를 만들었나

평소 MSA 패턴에 궁금증이 있었고, 실무에서 Nest.js 기반의 여러 서버를 운영하며
"다른 회사들은 Java/Spring 기반 MSA를 왜, 어떻게 운영할까?" 하는 질문이 생겼습니다.

단순한 CRUD 구현을 넘어 **"왜 이 기술을 쓰는가"**에 답할 수 있도록,
각 섹션마다 **문제 → 해결책 → 트레이드오프** 순서로 학습하며 구현했습니다.

---

## 아키텍처

```
                          [Client]
                             │
                             ▼
                    ┌─────────────────┐
                    │ Gateway (8080)  │  JWT 인증 + /internal 차단 + 라우팅
                    └────────┬────────┘
                             │ lb://
           ┌─────────────────┼─────────────────┐
           ▼                 ▼                 ▼
     ┌──────────┐      ┌──────────┐      ┌──────────┐
     │   User   │◀────▶│  Board   │◀────▶│  Point   │
     │  (8081)  │      │  (8082)  │      │  (8083)  │
     └────┬─────┘      └────┬─────┘      └────┬─────┘
          │                 │                 │
          ▼                 ▼                 ▼
       user-db           board-db         point-db

┌───────────────────── 인프라 ───────────────────────┐
│  Discovery (8761)    Kafka (9092)    Zipkin (9411)  │
│  Eureka Server       비동기 메시징   분산 추적       │
└─────────────────────────────────────────────────────┘
```

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language / Framework | Java 17, Spring Boot 3.5.x |
| 데이터 | Spring Data JPA, MySQL 8.0 |
| 서비스 디스커버리 | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| 인증 | JJWT (HS256) |
| 메시징 | Apache Kafka |
| 장애 복원력 | Resilience4j (CircuitBreaker, Fallback) |
| 분산 추적 | Micrometer Tracing + Brave + Zipkin |
| 테스트 | JUnit 5, Mockito, Spring Boot Test |
| 빌드/인프라 | Gradle, Docker Compose |

---

## 구현한 MSA 패턴

| # | 주제 | 핵심 |
|---|------|------|
| 1 | MSA 핵심 개념 | 독립 배포, 독립 DB, 서비스 간 통신 원칙 |
| 2 | 기본 마이크로서비스 구축 | User / Board 서비스, DB 분리, TDD |
| 3 | 동기 REST 통신 + CircuitBreaker | Fallback, Graceful Degradation |
| 4 | 쓰기 작업 체인 | 회원가입 → 포인트 적립, 게시글 → 포인트 차감 |
| 5 | Saga 패턴 (Choreography) | 보상 트랜잭션으로 데이터 정합성 |
| 6 | Kafka 비동기 메시징 | Producer/Consumer, 결합도 감소 |
| 7 | 이벤트 기반 데이터 동기화 | CQRS 맛보기, 읽기 최적화 |
| 8 | API Gateway | 단일 진입점, /internal 차단 |
| 9 | JWT 인증 (Gateway 통합) | Stateless 인증, X-User-Id 헤더 전파 |
| 10 | Service Discovery (Eureka) | 하드코딩 제거, Client-Side Load Balancing |
| 11 | 분산 추적 (Zipkin) | TraceId/SpanId, MDC 자동 전파 |

---

## 기술적 의사결정과 트레이드오프

### 1. 서비스 간 통신: REST vs Kafka
- **회원가입 → 포인트 적립 (REST)**: 즉시 성공 여부가 필요
- **게시글 작성 → 활동 점수 (Kafka)**: 실패해도 전체 흐름에 영향 없음
- 실패 전파가 치명적이지 않은 건 Kafka, 결과가 즉시 필요한 건 REST

### 2. 데이터 일관성: 분산 트랜잭션 대신 Saga
- 2PC는 블로킹/확장성 이슈. 대신 **Saga 보상 트랜잭션**
- 게시글 작성 실패 시 차감된 포인트를 환불하는 REFUND 타입으로 추적

### 3. 읽기 최적화: CQRS 맛보기
- Board Service의 자주 호출되는 목록 조회에 User Service REST 호출 제거
- 회원가입 이벤트로 Board DB에 UserInfo 로컬 캐싱 (Eventual Consistency)
- REST Fallback 남겨둠 → 이벤트 유실 시 안전망

### 4. 인증 중앙화
- Gateway에서 JWT 검증 후 X-User-Id 헤더로 전파
- 각 서비스는 인증 로직 없음, 받은 헤더만 신뢰
- 헤더 위조 방지: Gateway에서 기존 X-User-Id 제거 후 검증된 값으로 교체

### 5. JWT 페이로드 최소화
- userId만 포함, 이름·이메일 등 개인정보 제외 (JWT는 서명이지 암호화가 아님)
- 이름이 필요한 서비스는 DB 조회

### 6. 샘플링 비율
- 학습: 100% (`probability: 1.0`)
- 실무: 0.1~1% 수준이 일반적 — Zipkin 저장 비용 관리

---

## 실행 방법

### 1. 인프라 기동
```bash
docker compose up -d
# user-db, board-db, point-db, kafka, zookeeper, zipkin 기동
```

### 2. 서비스 기동 (순서 중요)
```bash
# 1. Discovery 먼저 (8761)
cd discovery-service && ./gradlew bootRun

# 2. 나머지 서비스 (각자 터미널에서)
cd user-service && ./gradlew bootRun      # 8081
cd board-service && ./gradlew bootRun     # 8082
cd point-service && ./gradlew bootRun     # 8083
cd gateway-service && ./gradlew bootRun   # 8080
```

### 3. 확인
- Eureka 대시보드: http://localhost:8761
- Zipkin UI: http://localhost:9411
- API 진입점: http://localhost:8080

### 4. 테스트 호출
```bash
# 회원가입 (Gateway 경유)
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@test.com","password":"password123"}'

# 로그인 → JWT 발급
TOKEN=$(curl -s -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"hong@test.com","password":"password123"}' \
  | jq -r .token)

# JWT로 보호된 API 호출
curl http://localhost:8080/users/1 -H "Authorization: Bearer $TOKEN"
```

---

## 디렉토리 구조

```
msa/
├── docker-compose.yml          # 인프라 (DB × 3, Kafka, Zookeeper, Zipkin)
├── discovery-service/          # Eureka Server (8761)
├── gateway-service/            # API Gateway (8080) - JWT 필터, 내부 API 차단
├── user-service/               # 사용자 (8081) - 회원가입, 로그인, JWT 발급
├── board-service/              # 게시글 (8082) - Saga, Kafka Producer, UserInfo 캐시
└── point-service/              # 포인트 (8083) - 적립/차감/환불, Kafka Consumer
```

---

## 테스트 커버리지

- **총 50+ 테스트** (서비스 계층, 컨트롤러, 이벤트 소비자, Gateway 필터)
- TDD 기반 개발: 테스트 먼저 → 구현 → 리뷰
- Mockito로 외부 의존성(RestTemplate, Kafka, JWT) 격리

---

## 학습 기록

각 섹션별 **개념 설명 · 사용자 질문 · 면접 Q&A**가 정리된 [노션 문서](https://www.notion.so/33ba054b7d8280a6a546e5bebaf26900)를 참고하세요.
