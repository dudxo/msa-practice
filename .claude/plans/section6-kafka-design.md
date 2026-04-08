# 설계 문서: 비동기 메시징 — Kafka 도입

## 1. 메타 정보

- 작성일: 2026-04-08
- 도메인: Board, Point (비동기 통신)
- 관련: MSA 학습 커리큘럼 섹션 6

---

## 2. 배경

- 섹션 4에서 게시글 작성 후 활동 점수 적립을 REST(동기)로 호출
- Point Service 장애 시 Fallback으로 적립을 포기 → 누락 가능성
- Kafka를 도입하여 "게시글 작성 완료" 이벤트를 발행하고, Point Service가 구독하여 적립 처리
- 이벤트가 Kafka에 보관되므로 Point Service 장애 시에도 복구 후 자동 처리

---

## 3. To-do 리스트

### 인프라
- [ ] 1. docker-compose.yml에 Kafka + Zookeeper 추가
  - 영향 파일: `docker-compose.yml`

### Board Service (Producer)
- [ ] 2. build.gradle에 spring-kafka 의존성 추가
  - 영향 파일: `board-service/build.gradle`
- [ ] 3. BoardCreatedEvent DTO 생성
  - 영향 파일: `board-service/src/main/java/com/msa/board/event/BoardCreatedEvent.java`
- [ ] 4. BoardEventProducer 생성 (Kafka 이벤트 발행)
  - 영향 파일: `board-service/src/main/java/com/msa/board/event/BoardEventProducer.java`
- [ ] 5. BoardService.createBoard 수정 — REST earnPoints를 Kafka 이벤트로 전환
  - 영향 파일: `board-service/src/main/java/com/msa/board/service/BoardService.java`
- [ ] 6. application.yml에 Kafka 설정 추가
  - 영향 파일: `board-service/src/main/resources/application.yml`

### Point Service (Consumer)
- [ ] 7. build.gradle에 spring-kafka 의존성 추가
  - 영향 파일: `point-service/build.gradle`
- [ ] 8. BoardCreatedEvent DTO 생성 (Board와 동일 구조)
  - 영향 파일: `point-service/src/main/java/com/msa/point/event/BoardCreatedEvent.java`
- [ ] 9. BoardEventConsumer 생성 (Kafka 이벤트 구독 → 활동 점수 적립)
  - 영향 파일: `point-service/src/main/java/com/msa/point/event/BoardEventConsumer.java`
- [ ] 10. application.yml에 Kafka 설정 추가
  - 영향 파일: `point-service/src/main/resources/application.yml`

---

## 4. API 설계서

API 변경 없음. Kafka 이벤트로 내부 통신만 전환.

### 이벤트 설계

**Topic**: `board-created`

**이벤트 페이로드**:
```json
{
  "boardId": 1,
  "authorId": 1,
  "title": "첫 번째 게시글",
  "createdAt": "2026-04-08T12:00:00"
}
```

---

## 5. DB 설계서

스키마 변경 없음.

---

## 6. 테스트 관점

### Board Service (Producer)
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-BOARD-015 | unit | 게시글 작성 후 Kafka 이벤트 발행됨 | BoardEventProducer.publish 호출 확인 |

### Point Service (Consumer)
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-POINT-007 | unit | board-created 이벤트 수신 시 10P 적립 | PointService.earn 호출 확인 |

## 7. 변경 전후 비교

### Before (섹션 4 — REST)
```
Board Service → REST → Point Service: earn 10P
→ Point Service 장애 시 Fallback으로 포기 (누락 가능)
```

### After (섹션 6 — Kafka)
```
Board Service → Kafka: "게시글 작성됨" 이벤트 발행
Point Service → Kafka: 이벤트 구독 → 10P 적립
→ Point Service 장애 시 이벤트는 Kafka에 보관 → 복구 후 자동 처리
```
