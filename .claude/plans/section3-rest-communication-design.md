# 설계 문서: 서비스 간 REST 통신 (데이터 조회) + CircuitBreaker

## 1. 메타 정보

- 작성일: 2026-04-07
- 도메인: Board, User (서비스 간 통신)
- 관련: MSA 학습 커리큘럼 섹션 3

---

## 2. 배경

- 현재 게시글 조회 시 `authorId`만 반환되어 작성자가 누구인지 알 수 없음
- Board DB와 User DB가 분리되어 있으므로 JOIN 불가 → Board Service가 User Service API를 호출해야 함
- User Service 장애 시 Board Service까지 장애가 전파되는 것을 방지해야 함

---

## 3. To-do 리스트

### User Service (내부 API 추가)
- [ ] 1. 사용자 다건 조회 API 추가 (`POST /internal/users/by-ids`)
  - 영향 파일: `user-service/src/main/java/com/msa/user/controller/UserInternalController.java`
  - 영향 파일: `user-service/src/main/java/com/msa/user/service/UserService.java`

### Board Service (서비스 간 통신 + CircuitBreaker)
- [ ] 2. build.gradle에 Resilience4j 의존성 추가
  - 영향 파일: `board-service/build.gradle`
- [ ] 3. RestTemplate Bean 설정 (Timeout 포함)
  - 영향 파일: `board-service/src/main/java/com/msa/board/config/RestTemplateConfig.java`
- [ ] 4. UserServiceClient 생성 (User Service 호출 + CircuitBreaker + Fallback)
  - 영향 파일: `board-service/src/main/java/com/msa/board/client/UserServiceClient.java`
- [ ] 5. BoardResponse에 authorName 필드 추가
  - 영향 파일: `board-service/src/main/java/com/msa/board/dto/BoardResponse.java`
- [ ] 6. BoardService 수정 — 게시글 조회 시 작성자 정보 포함
  - 영향 파일: `board-service/src/main/java/com/msa/board/service/BoardService.java`
- [ ] 7. application.yml에 Resilience4j 설정 추가
  - 영향 파일: `board-service/src/main/resources/application.yml`

---

## 4. API 설계서

### 4.1 사용자 다건 조회 (내부 API)

#### `POST /internal/users/by-ids`

**설명**: 여러 사용자 ID로 사용자 정보를 한 번에 조회합니다. 서비스 간 내부 통신 전용.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| body | List<Long> | O | 조회할 사용자 ID 목록 |

**요청 예시**
```json
[1, 2, 3]
```

**응답 (200 OK)**
```json
[
  { "id": 1, "name": "홍길동", "email": "hong@test.com", "createdAt": "..." },
  { "id": 2, "name": "김철수", "email": "kim@test.com", "createdAt": "..." }
]
```

### 4.2 게시글 단건 조회 (수정)

#### `GET /boards/{id}`

**응답 (200 OK)** — authorName 추가
```json
{
  "id": 1,
  "title": "첫 번째 게시글",
  "content": "안녕하세요!",
  "authorId": 1,
  "authorName": "홍길동",
  "createdAt": "2026-04-07T12:00:00"
}
```

**User Service 장애 시 Fallback 응답**
```json
{
  "id": 1,
  "title": "첫 번째 게시글",
  "content": "안녕하세요!",
  "authorId": 1,
  "authorName": "알 수 없음",
  "createdAt": "2026-04-07T12:00:00"
}
```

### 4.3 게시글 전체 조회 (수정)

#### `GET /boards`

**응답 (200 OK)** — authorName 추가, Batch API로 N+1 방지
```json
[
  { "id": 1, "title": "제목1", "authorId": 1, "authorName": "홍길동", ... },
  { "id": 2, "title": "제목2", "authorId": 1, "authorName": "홍길동", ... }
]
```

---

## 5. DB 설계서

스키마 변경 없음.

---

## 6. 테스트 관점

### User Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-USER-007 | unit | 존재하는 ID 목록으로 다건 조회 | UserResponse 리스트 반환 |
| T-USER-008 | unit | 빈 리스트로 다건 조회 | 빈 리스트 반환 |

### Board Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-BOARD-006 | unit | 게시글 단건 조회 시 authorName 포함 | authorName = "홍길동" |
| T-BOARD-007 | unit | User Service 장애 시 Fallback | authorName = "알 수 없음" |
| T-BOARD-008 | unit | 게시글 전체 조회 시 authorName 포함 (Batch) | 모든 게시글에 authorName |
| T-BOARD-009 | unit | 전체 조회 중 User Service 장애 시 Fallback | 모든 authorName = "알 수 없음" |

## 7. CircuitBreaker 설정

| 설정 | 값 | 설명 |
|-----|---|------|
| slidingWindowSize | 10 | 최근 10개 요청 기준 |
| failureRateThreshold | 50 | 50% 이상 실패 시 OPEN |
| waitDurationInOpenState | 10s | OPEN 후 10초 대기 후 HALF_OPEN |
| permittedNumberOfCallsInHalfOpenState | 3 | HALF_OPEN에서 3개 시도 |
| connectTimeout | 3s | 연결 타임아웃 |
| readTimeout | 3s | 읽기 타임아웃 |
