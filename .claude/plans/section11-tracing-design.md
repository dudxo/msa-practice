# 설계 문서: 분산 추적 & 모니터링 (Zipkin + Micrometer)

## 1. 메타 정보

- 작성일: 2026-04-13
- 도메인: 전체 서비스 (분산 추적)
- 관련: MSA 학습 커리큘럼 섹션 11

---

## 2. 배경

- MSA는 한 요청이 여러 서비스를 거쳐 처리 → 단일 로그로 지연 원인 식별 어려움
- TraceId를 모든 서비스에 전파하고, Span 단위로 구간별 소요시간 측정 필요
- Spring Boot 3.x의 Micrometer Tracing + Brave + Zipkin 조합 채택
- 샘플링 비율은 학습용 1.0, 실무는 0.1~1% 권장

---

## 3. To-do 리스트

### 인프라
- [ ] 1. docker-compose.yml에 Zipkin 컨테이너 추가 (openzipkin/zipkin:3, 9411)
  - 영향 파일: `docker-compose.yml`

### 각 서비스 의존성
- [ ] 2. 4개 서비스 build.gradle에 의존성 추가
  - `spring-boot-starter-actuator`
  - `io.micrometer:micrometer-tracing-bridge-brave`
  - `io.zipkin.reporter2:zipkin-reporter-brave`
  - 영향 파일: user/board/point/gateway-service/build.gradle

### 각 서비스 설정
- [ ] 3. application.yml에 tracing 설정 추가
  - `management.tracing.sampling.probability: 1.0`
  - `management.zipkin.tracing.endpoint: http://localhost:9411/api/v2/spans`
- [ ] 4. 로그 패턴에 traceId/spanId 포함
  - `logging.pattern.level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"`

---

## 4. API 설계서

API 변경 없음. 내부 헤더 자동 추가(B3 Propagation).

### 자동 추가되는 헤더
| 헤더 | 설명 |
|---|---|
| X-B3-TraceId | 요청 전체에서 동일한 추적 ID |
| X-B3-SpanId | 현재 작업 단위 ID |
| X-B3-ParentSpanId | 상위 작업의 SpanId |

Micrometer가 RestTemplate/WebClient/Kafka 호출 시 자동으로 첨부.

---

## 5. DB 설계서

DB 변경 없음. 추적 데이터는 Zipkin이 별도 저장(메모리/MySQL/Elasticsearch 선택).

---

## 6. 테스트 관점

### 수동 검증 (통합)
| 시나리오 | 기대 결과 |
|---|---|
| Zipkin UI(9411) 접속 | 서비스 목록에 4개 서비스 노출 |
| Gateway → `/boards` 호출 | 하나의 TraceId 아래 여러 Span 수집 |
| Trace 상세 보기 | Gateway → Board → User 호출 체인 시각화 |
| 로그 확인 | 서비스명/traceId/spanId가 로그에 포함 |

### MDC 관련 주의
- `@Async`, Kafka Consumer 등 비동기 처리는 스레드 변경 시 MDC 자동 전파 안 됨
- Micrometer가 대부분 자동 처리하나, 직접 Executor 생성 시 TaskDecorator 필요

---

## 7. 변경 전후 비교

### Before
```
2026-04-13 16:00  INFO  [user-service] 게시글 조회
2026-04-13 16:00  INFO  [board-service] 게시글 조회
→ 어느 요청의 로그인지 구분 불가
```

### After
```
2026-04-13 16:00  INFO [user-service,abc123,789xyz]  게시글 조회
2026-04-13 16:00  INFO [board-service,abc123,456abc] 게시글 조회
→ traceId(abc123)로 전체 흐름 추적 가능
```

### Zipkin UI에서의 인사이트 예시
```
TraceId: 69dc9cc6...                총 512ms
├─ gateway-service                  473ms
│  └─ board-service /boards         337ms
│     ├─ user-service /users/{id}   70ms  ← REST Fallback 1
│     ├─ user-service /users/{id}   8ms   ← REST Fallback 2
│     └─ user-service /users/{id}   5ms   ← REST Fallback 3
```
→ Board의 getAllBoards가 로컬 캐시 미스 시 User Service를 개별 호출하고 있어 N+1 유사 이슈 발견 가능.
