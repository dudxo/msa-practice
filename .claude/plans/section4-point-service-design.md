# 설계 문서: Point Service + 데이터 쓰기 (서비스 간 REST 통신)

## 1. 메타 정보

- 작성일: 2026-04-08
- 도메인: Point, User, Board (서비스 간 쓰기 통신)
- 관련: MSA 학습 커리큘럼 섹션 4

---

## 2. 배경

- 게시글 작성 시 포인트 차감 → 게시글 생성 → 활동 점수 적립 순서로 처리해야 함
- 회원가입 시 가입 축하 포인트를 적립해야 함
- Point Service를 독립 마이크로서비스로 분리하여 포인트/활동 점수를 관리
- 이번 섹션에서는 정상 흐름만 구현 (실패 시 보상 트랜잭션은 섹션 5에서)

---

## 3. To-do 리스트

### Point Service 신규 생성 (포트 8083)
- [ ] 1. IntelliJ에서 point-service 프로젝트 생성
- [ ] 2. docker-compose.yml에 point-db 추가 (포트 3323)
- [ ] 3. application.yml 설정
- [ ] 4. PointHistory 엔티티 생성 (포인트 이력)
  - 영향 파일: `point-service/src/main/java/com/msa/point/entity/PointHistory.java`
- [ ] 5. PointBalance 엔티티 생성 (잔액 관리)
  - 영향 파일: `point-service/src/main/java/com/msa/point/entity/PointBalance.java`
- [ ] 6. Repository 생성
  - 영향 파일: `point-service/src/main/java/com/msa/point/repository/`
- [ ] 7. DTO 생성 (PointRequest, PointResponse, BalanceResponse)
  - 영향 파일: `point-service/src/main/java/com/msa/point/dto/`
- [ ] 8. PointService 생성 (적립, 차감, 잔액 조회)
  - 영향 파일: `point-service/src/main/java/com/msa/point/service/PointService.java`
- [ ] 9. PointController 생성
  - 영향 파일: `point-service/src/main/java/com/msa/point/controller/PointController.java`
- [ ] 10. GlobalExceptionHandler 생성
  - 영향 파일: `point-service/src/main/java/com/msa/point/exception/`

### User Service 수정 (회원가입 시 포인트 적립)
- [ ] 11. PointServiceClient 생성 (User → Point 호출)
  - 영향 파일: `user-service/src/main/java/com/msa/user/client/PointServiceClient.java`
- [ ] 12. UserService.createUser 수정 — 회원가입 후 포인트 적립 호출
  - 영향 파일: `user-service/src/main/java/com/msa/user/service/UserService.java`
- [ ] 13. build.gradle에 Resilience4j 의존성 추가
  - 영향 파일: `user-service/build.gradle`

### Board Service 수정 (게시글 작성 시 포인트 차감 + 활동 점수 적립)
- [ ] 14. PointServiceClient 생성 (Board → Point 호출)
  - 영향 파일: `board-service/src/main/java/com/msa/board/client/PointServiceClient.java`
- [ ] 15. BoardService.createBoard 수정 — 포인트 차감 → 게시글 생성 → 활동 점수 적립
  - 영향 파일: `board-service/src/main/java/com/msa/board/service/BoardService.java`

---

## 4. API 설계서

### 4.1 포인트 적립

#### `POST /points/earn`

**설명**: 포인트를 적립합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| userId | Long (body) | O | 대상 사용자 ID |
| amount | Integer (body) | O | 적립 포인트 (양수) |
| description | String (body) | O | 적립 사유 |

**요청 예시**
```json
{ "userId": 1, "amount": 500, "description": "가입 축하 포인트" }
```

**응답 (201 Created)**
```json
{
  "id": 1,
  "userId": 1,
  "amount": 500,
  "type": "EARN",
  "description": "가입 축하 포인트",
  "balance": 500,
  "createdAt": "2026-04-08T12:00:00"
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 400 | amount가 0 이하 |
| 400 | userId 또는 description이 null |

### 4.2 포인트 차감

#### `POST /points/deduct`

**설명**: 포인트를 차감합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| userId | Long (body) | O | 대상 사용자 ID |
| amount | Integer (body) | O | 차감 포인트 (양수) |
| description | String (body) | O | 차감 사유 |

**요청 예시**
```json
{ "userId": 1, "amount": 100, "description": "게시글 작성" }
```

**응답 (201 Created)**
```json
{
  "id": 2,
  "userId": 1,
  "amount": 100,
  "type": "DEDUCT",
  "description": "게시글 작성",
  "balance": 400,
  "createdAt": "2026-04-08T12:01:00"
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 400 | amount가 0 이하 |
| 400 | 잔액 부족 (현재 잔액 < 차감 금액) |

### 4.3 잔액 조회

#### `GET /points/balance/{userId}`

**설명**: 사용자의 포인트 잔액을 조회합니다.

**응답 (200 OK)**
```json
{
  "userId": 1,
  "balance": 400
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 404 | 해당 userId의 잔액 정보가 없음 |

---

## 5. DB 설계서

### 5.1 테이블: `point_histories` (Point DB - 포트 3323)

| 컬럼 | 타입 | 제약조건 | 설명 |
|-----|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 이력 ID |
| user_id | BIGINT | NOT NULL | 사용자 ID |
| amount | INT | NOT NULL | 포인트 금액 (양수) |
| type | VARCHAR(10) | NOT NULL | EARN 또는 DEDUCT |
| description | VARCHAR(255) | NOT NULL | 사유 |
| created_at | DATETIME | NOT NULL | 생성일시 |

### 5.2 테이블: `point_balances` (Point DB - 포트 3323)

| 컬럼 | 타입 | 제약조건 | 설명 |
|-----|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ID |
| user_id | BIGINT | NOT NULL, UNIQUE | 사용자 ID |
| balance | INT | NOT NULL, DEFAULT 0 | 현재 잔액 |

### 5.3 인덱스

- `idx_point_histories_user_id` on `point_histories(user_id)` — 사용자별 이력 조회
- `point_balances.user_id` UNIQUE — 사용자당 잔액 레코드 1개

---

## 6. 테스트 관점

### Point Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-POINT-001 | unit | 정상 포인트 적립 | 이력 생성 + 잔액 증가 |
| T-POINT-002 | unit | 정상 포인트 차감 | 이력 생성 + 잔액 감소 |
| T-POINT-003 | unit | 잔액 부족 시 차감 | 예외 발생 |
| T-POINT-004 | unit | 잔액 조회 | 현재 잔액 반환 |
| T-POINT-005 | unit | 최초 적립 시 PointBalance 자동 생성 | balance 레코드 생성 |

### User Service (수정분)
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-USER-009 | unit | 회원가입 후 포인트 적립 호출됨 | PointServiceClient.earn 호출 확인 |

### Board Service (수정분)
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-BOARD-010 | unit | 게시글 작성 시 포인트 차감 → 게시글 생성 → 활동 점수 적립 순서 | 순서대로 호출 확인 |
| T-BOARD-011 | unit | 포인트 부족 시 게시글 작성 실패 | 예외 발생, 게시글 미생성 |

## 7. 서비스 간 호출 흐름

### 회원가입
```
Client → User Service
           1. 사용자 생성 (User DB)
           2. Point Service 호출: POST /points/earn (500P 가입 축하)
         ← 응답
```

### 게시글 작성
```
Client → Board Service
           1. Point Service 호출: POST /points/deduct (100P 차감)
           2. 게시글 생성 (Board DB)
           3. Point Service 호출: POST /points/earn (10P 활동 점수)
         ← 응답
```
