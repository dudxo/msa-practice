# 설계 문서: API Gateway — Spring Cloud Gateway

## 1. 메타 정보

- 작성일: 2026-04-10
- 도메인: Gateway (단일 진입점)
- 관련: MSA 학습 커리큘럼 섹션 8

---

## 2. 배경

- 클라이언트가 3개 서비스(User/Board/Point)의 포트를 각각 알아야 하는 문제
- `/internal/users/by-ids` 같은 내부 API가 외부에 노출
- 각 서비스가 인증/로깅/CORS 등 횡단 관심사를 중복 구현할 가능성
- Spring Cloud Gateway를 앞에 두고 단일 진입점 + 필터 체인으로 해결

---

## 3. To-do 리스트

### 프로젝트 세팅
- [ ] 1. gateway-service 프로젝트 생성 (Spring Cloud Gateway, WebFlux 기반)
  - 영향 파일: `gateway-service/build.gradle`

### 라우팅
- [ ] 2. application.yml에 3개 서비스 라우트 설정
  - 영향 파일: `gateway-service/src/main/resources/application.yml`
  - 라우트: `/users/**`, `/boards/**`, `/points/**`

### 내부 API 차단
- [ ] 3. InternalApiFilter (WebFilter) 구현 — `/internal/**` 경로 403
  - 영향 파일: `gateway-service/src/main/java/com/msa/gateway/filter/InternalApiFilter.java`
  - order: -1 (라우팅 전 최우선 실행)

---

## 4. API 설계서

Gateway 자체 API 없음. 라우팅만 수행.

### 외부 접근 허용 경로
| 메서드 | 경로 | 라우팅 대상 |
|---|---|---|
| * | /users/** | user-service (8081) |
| * | /boards/** | board-service (8082) |
| * | /points/** | point-service (8083) |

### 차단 경로
| 경로 | 응답 |
|---|---|
| /internal/** | 403 Forbidden |

---

## 5. DB 설계서

DB 없음.

---

## 6. 테스트 관점

| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-GW-001 | integration | `/internal/users/by-ids` 외부 요청 | 403 Forbidden |
| T-GW-002 | integration | `/internal/anything` 외부 요청 | 403 Forbidden |
| T-GW-003 | integration | user-service 라우트 등록 확인 | RouteLocator에 존재 |
| T-GW-004 | integration | board-service 라우트 등록 확인 | RouteLocator에 존재 |
| T-GW-005 | integration | point-service 라우트 등록 확인 | RouteLocator에 존재 |

---

## 7. 변경 전후 비교

### Before
```
클라이언트 → http://localhost:8081/users (User)
클라이언트 → http://localhost:8082/boards (Board)
클라이언트 → http://localhost:8081/internal/users/by-ids (외부 노출!)
```

### After
```
클라이언트 → http://localhost:8080/users   → User Service
클라이언트 → http://localhost:8080/boards  → Board Service
클라이언트 → http://localhost:8080/internal/** → 403 차단
```
