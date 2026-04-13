# 설계 문서: 이벤트 기반 데이터 동기화 (CQRS 맛보기)

## 1. 메타 정보

- 작성일: 2026-04-09
- 도메인: User, Board (읽기 최적화)
- 관련: MSA 학습 커리큘럼 섹션 7

---

## 2. 배경

- 게시글 목록 조회마다 User Service를 REST로 호출하는 성능 병목
- User Service 장애 시 작성자 이름이 "알 수 없음"으로 표시됨 (UX 저하)
- 회원가입 이벤트를 Kafka로 발행해 Board Service가 사용자 정보를 로컬 캐싱
- 읽기는 로컬 DB에서, 쓰기는 원본(User Service)에서 → CQRS 개념 도입

---

## 3. To-do 리스트

### User Service (Producer)
- [ ] 1. build.gradle에 spring-kafka 의존성 추가
  - 영향 파일: `user-service/build.gradle`
- [ ] 2. UserCreatedEvent DTO 생성 (userId, name, email)
  - 영향 파일: `user-service/src/main/java/com/msa/user/event/UserCreatedEvent.java`
- [ ] 3. UserEventProducer 생성 (user-created 토픽 발행)
  - 영향 파일: `user-service/src/main/java/com/msa/user/event/UserEventProducer.java`
- [ ] 4. UserService.createUser 수정 — 저장 후 이벤트 발행
  - 영향 파일: `user-service/src/main/java/com/msa/user/service/UserService.java`
- [ ] 5. application.yml에 Kafka Producer 설정 추가
  - 영향 파일: `user-service/src/main/resources/application.yml`

### Board Service (Consumer + 로컬 캐시)
- [ ] 6. UserInfo 엔티티 생성 (userId unique, name, email)
  - 영향 파일: `board-service/src/main/java/com/msa/board/entity/UserInfo.java`
- [ ] 7. UserInfoRepository 생성 (findByUserId, findAllByUserIdIn)
  - 영향 파일: `board-service/src/main/java/com/msa/board/repository/UserInfoRepository.java`
- [ ] 8. UserCreatedEvent DTO 생성 (User Service와 동일 구조)
  - 영향 파일: `board-service/src/main/java/com/msa/board/event/UserCreatedEvent.java`
- [ ] 9. UserEventConsumer 생성 (멱등성: find 후 update/insert)
  - 영향 파일: `board-service/src/main/java/com/msa/board/event/UserEventConsumer.java`
- [ ] 10. BoardService 수정 — resolveAuthorName 메서드 도입 (로컬 우선 → REST Fallback)
  - 영향 파일: `board-service/src/main/java/com/msa/board/service/BoardService.java`
- [ ] 11. application.yml에 Kafka Consumer 설정 추가 (groupId: board-service)
  - 영향 파일: `board-service/src/main/resources/application.yml`

---

## 4. API 설계서

외부 API 변경 없음. Board 조회 응답은 동일하나 내부 구현이 로컬 캐시 우선으로 바뀜.

### 이벤트 설계

**Topic**: `user-created`

**이벤트 페이로드**:
```json
{
  "userId": 1,
  "name": "홍길동",
  "email": "hong@test.com"
}
```

---

## 5. DB 설계서

### board_db 스키마 추가

```sql
CREATE TABLE user_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  name VARCHAR(20) NOT NULL,
  email VARCHAR(255) NOT NULL
);
```

---

## 6. 테스트 관점

### User Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-USER-010 | unit | 회원가입 시 user-created 이벤트 발행 | UserEventProducer.publishUserCreated 호출 |

### Board Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-BOARD-016 | unit | user-created 이벤트 수신 → 신규 저장 | UserInfoRepository.save 호출 |
| T-BOARD-017 | unit | 중복 이벤트 수신 → update (멱등성) | 기존 엔티티 update 호출 |
| T-BOARD-018 | unit | getBoard 시 로컬 UserInfo 우선 사용 | userServiceClient.getUserName 미호출 |
| T-BOARD-019 | unit | 로컬 없으면 REST Fallback | userServiceClient.getUserName 호출 |
| T-BOARD-020 | unit | getAllBoards 로컬 + REST 혼합 | 로컬 있는 건 DB, 없는 건 개별 REST |

---

## 7. 변경 전후 비교

### Before
```
게시글 조회 → Board Service → REST 호출 → User Service (매번)
User Service 장애 시 "알 수 없음" 표시
```

### After
```
회원가입 → User Service → Kafka("user-created") → Board Service → 로컬 DB 저장

게시글 조회 → Board Service → 로컬 UserInfo 조회 (REST 호출 없음)
로컬에 없으면 → REST Fallback (안전망)
```
