# 설계 문서: Saga 패턴 + 보상 트랜잭션

## 1. 메타 정보

- 작성일: 2026-04-08
- 도메인: Board, Point (분산 트랜잭션)
- 관련: MSA 학습 커리큘럼 섹션 5

---

## 2. 배경

- 섹션 4에서 게시글 작성 흐름: 포인트 차감 → 게시글 생성 → 활동 점수 적립
- 문제: 포인트 차감 후 게시글 생성이 실패하면 포인트만 차감되고 게시글은 없음 (데이터 불일치)
- 각 서비스가 다른 DB를 사용하므로 @Transactional로 묶을 수 없음
- Saga 패턴(Orchestration 방식)으로 실패 시 보상 트랜잭션을 자동 실행하여 데이터 정합성 유지

---

## 3. To-do 리스트

### Point Service (보상 API 추가)
- [ ] 1. PointType에 REFUND 추가
  - 영향 파일: `point-service/src/main/java/com/msa/point/entity/PointType.java`
- [ ] 2. PointService에 refund 메서드 추가
  - 영향 파일: `point-service/src/main/java/com/msa/point/service/PointService.java`
- [ ] 3. PointController에 refund API 추가
  - 영향 파일: `point-service/src/main/java/com/msa/point/controller/PointController.java`

### Board Service (Saga Orchestration 적용)
- [ ] 4. PointServiceClient에 refundPoints 메서드 추가
  - 영향 파일: `board-service/src/main/java/com/msa/board/client/PointServiceClient.java`
- [ ] 5. BoardService.createBoard에 Saga 패턴 적용 (try-catch + 보상)
  - 영향 파일: `board-service/src/main/java/com/msa/board/service/BoardService.java`

---

## 4. API 설계서

### 4.1 포인트 환불 (보상 트랜잭션용)

#### `POST /points/refund`

**설명**: Saga 보상 트랜잭션으로 차감된 포인트를 환불합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| userId | Long (body) | O | 대상 사용자 ID |
| amount | Integer (body) | O | 환불 포인트 (양수) |
| description | String (body) | O | 환불 사유 |

**요청 예시**
```json
{ "userId": 1, "amount": 100, "description": "게시글 작성 실패 환불" }
```

**응답 (201 Created)**
```json
{
  "id": 5,
  "userId": 1,
  "amount": 100,
  "type": "REFUND",
  "description": "게시글 작성 실패 환불",
  "balance": 500,
  "createdAt": "2026-04-08T12:00:00"
}
```

### 4.2 게시글 작성 (Saga 적용 — 기존 API 동작 변경)

#### `POST /boards`

**정상 흐름 (변경 없음)**
```
① 포인트 차감 → ② 게시글 생성 → ③ 활동 점수 적립 → 응답
```

**실패 흐름 (신규 — Saga 보상)**
```
① 포인트 차감 ✅ → ② 게시글 생성 ❌ → ③ 포인트 환불 (보상) → 예외 전파
```

---

## 5. DB 설계서

### 5.1 PointType enum 변경

| 값 | 설명 | 변경 |
|---|------|------|
| EARN | 적립 | 기존 |
| DEDUCT | 차감 | 기존 |
| REFUND | 환불 (보상 트랜잭션) | **추가** |

스키마 변경 없음 (VARCHAR 컬럼이므로 enum 값 추가만)

---

## 6. 테스트 관점

### Point Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-POINT-006 | unit | 정상 환불 시 이력 생성 + 잔액 증가 | type=REFUND, 잔액 복구 |

### Board Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-BOARD-012 | unit | 게시글 생성 실패 시 포인트 환불(보상) 호출됨 | refundPoints 호출 확인 |
| T-BOARD-013 | unit | 게시글 생성 실패 + 환불 성공 시 예외는 여전히 전파 | 클라이언트에 에러 반환 |
| T-BOARD-014 | unit | 정상 흐름에서는 환불이 호출되지 않음 | refundPoints never 호출 |

## 7. Saga 흐름 다이어그램

### 정상 흐름
```
Board Service (Orchestrator)
  ① → Point Service: deduct 100P    ✅
  ② → Board DB: save board          ✅
  ③ → Point Service: earn 10P       ✅ (or Fallback)
  → 성공 응답
```

### 실패 + 보상 흐름
```
Board Service (Orchestrator)
  ① → Point Service: deduct 100P    ✅ (차감됨)
  ② → Board DB: save board          ❌ (실패!)
  ③ → Point Service: refund 100P    ✅ (보상 — 차감 되돌림)
  → 에러 응답 (클라이언트에게 실패 알림)
```
