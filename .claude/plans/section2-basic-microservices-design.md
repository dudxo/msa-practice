# 설계 문서: User Service + Board Service 기본 구축

## 1. 메타 정보

- 작성일: 2026-04-07
- 도메인: User, Board
- 관련: MSA 학습 커리큘럼 섹션 2

---

## 2. 배경 (왜 이 기능이 필요한가?)

- MSA 프로젝트의 첫 번째 구현 단계로, 가장 기본이 되는 User Service와 Board Service를 구축한다.
- 각 서비스가 독립 DB를 갖고, 독립적으로 실행되는 구조를 만들어 MSA의 핵심 원칙을 체감한다.
- 이후 섹션(서비스 간 통신, Saga, Kafka 등)의 토대가 되는 서비스이다.

---

## 3. To-do 리스트

### User Service (포트 8081)
- [ ] 1. application.yml 설정 (MySQL 연결, 포트 8081)
  - 영향 파일: `user-service/src/main/resources/application.yml`
- [ ] 2. User 엔티티 생성
  - 영향 파일: `user-service/src/main/java/com/msa/user/entity/User.java`
- [ ] 3. UserRepository 생성
  - 영향 파일: `user-service/src/main/java/com/msa/user/repository/UserRepository.java`
- [ ] 4. DTO 생성 (CreateUserRequest, UserResponse)
  - 영향 파일: `user-service/src/main/java/com/msa/user/dto/`
- [ ] 5. UserService 생성 (회원가입, 조회)
  - 영향 파일: `user-service/src/main/java/com/msa/user/service/UserService.java`
- [ ] 6. UserController 생성 (REST API)
  - 영향 파일: `user-service/src/main/java/com/msa/user/controller/UserController.java`
- [ ] 7. GlobalExceptionHandler 생성
  - 영향 파일: `user-service/src/main/java/com/msa/user/exception/`

### Board Service (포트 8082)
- [ ] 8. application.yml 설정 (MySQL 연결, 포트 8082)
  - 영향 파일: `board-service/src/main/resources/application.yml`
- [ ] 9. Board 엔티티 생성
  - 영향 파일: `board-service/src/main/java/com/msa/board/entity/Board.java`
- [ ] 10. BoardRepository 생성
  - 영향 파일: `board-service/src/main/java/com/msa/board/repository/BoardRepository.java`
- [ ] 11. DTO 생성 (CreateBoardRequest, BoardResponse)
  - 영향 파일: `board-service/src/main/java/com/msa/board/dto/`
- [ ] 12. BoardService 생성 (게시글 CRUD)
  - 영향 파일: `board-service/src/main/java/com/msa/board/service/BoardService.java`
- [ ] 13. BoardController 생성 (REST API)
  - 영향 파일: `board-service/src/main/java/com/msa/board/controller/BoardController.java`
- [ ] 14. GlobalExceptionHandler 생성
  - 영향 파일: `board-service/src/main/java/com/msa/board/exception/`

---

## 4. API 설계서

### 4.1 회원가입

#### `POST /users`

**설명**: 새로운 사용자를 등록합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| name | string (body) | O | 사용자 이름 (2~20자) |
| email | string (body) | O | 이메일 (형식 검증) |

**응답 (201 Created)**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@test.com",
  "createdAt": "2026-04-07T12:00:00"
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 400 | 이름이 비어있거나 2자 미만/20자 초과 |
| 400 | 이메일 형식이 올바르지 않음 |
| 409 | 이미 등록된 이메일 |

### 4.2 사용자 조회

#### `GET /users/{id}`

**설명**: 특정 사용자 정보를 조회합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| id | Long (path) | O | 사용자 ID |

**응답 (200 OK)**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@test.com",
  "createdAt": "2026-04-07T12:00:00"
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 404 | 해당 ID의 사용자가 존재하지 않음 |

### 4.3 게시글 작성

#### `POST /boards`

**설명**: 새로운 게시글을 작성합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| title | string (body) | O | 게시글 제목 (1~100자) |
| content | string (body) | O | 게시글 내용 (1자 이상) |
| authorId | Long (body) | O | 작성자 User ID |

**응답 (201 Created)**
```json
{
  "id": 1,
  "title": "첫 번째 게시글",
  "content": "안녕하세요!",
  "authorId": 1,
  "createdAt": "2026-04-07T12:00:00"
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 400 | 제목이 비어있거나 100자 초과 |
| 400 | 내용이 비어있음 |
| 400 | authorId가 null |

### 4.4 게시글 단건 조회

#### `GET /boards/{id}`

**설명**: 특정 게시글을 조회합니다.

**요청**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| id | Long (path) | O | 게시글 ID |

**응답 (200 OK)**
```json
{
  "id": 1,
  "title": "첫 번째 게시글",
  "content": "안녕하세요!",
  "authorId": 1,
  "createdAt": "2026-04-07T12:00:00"
}
```

**에러 케이스**
| 코드 | 상황 |
|-----|-----|
| 404 | 해당 ID의 게시글이 존재하지 않음 |

### 4.5 게시글 전체 조회

#### `GET /boards`

**설명**: 전체 게시글 목록을 조회합니다.

**응답 (200 OK)**
```json
[
  {
    "id": 1,
    "title": "첫 번째 게시글",
    "content": "안녕하세요!",
    "authorId": 1,
    "createdAt": "2026-04-07T12:00:00"
  }
]
```

---

## 5. DB 설계서

### 5.1 테이블: `users` (User DB - 포트 3321)

| 컬럼 | 타입 | 제약조건 | 설명 |
|-----|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 ID |
| name | VARCHAR(20) | NOT NULL | 사용자 이름 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 이메일 |
| created_at | DATETIME | NOT NULL | 가입일시 |

### 5.2 테이블: `boards` (Board DB - 포트 3322)

| 컬럼 | 타입 | 제약조건 | 설명 |
|-----|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 게시글 ID |
| title | VARCHAR(100) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| author_id | BIGINT | NOT NULL | 작성자 User ID (FK 없음, 독립 DB) |
| created_at | DATETIME | NOT NULL | 작성일시 |

### 5.3 관계

- `boards.author_id`는 `users.id`를 참조하지만 **FK를 걸지 않음** (서로 다른 DB이므로)
- 데이터 정합성은 애플리케이션 레벨에서 관리 (섹션 3에서 서비스 간 통신으로 검증 추가 예정)

---

## 6. 테스트 관점

### User Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-USER-001 | unit | 정상 요청으로 회원가입 | 성공, UserResponse 반환 |
| T-USER-002 | unit | 이메일 중복으로 회원가입 | 409 예외 발생 |
| T-USER-003 | unit | 존재하는 ID로 사용자 조회 | 성공, UserResponse 반환 |
| T-USER-004 | unit | 존재하지 않는 ID로 사용자 조회 | 404 예외 발생 |
| T-USER-005 | unit | 이름이 빈값으로 회원가입 | 400 유효성 검증 실패 |
| T-USER-006 | unit | 이메일 형식이 잘못된 회원가입 | 400 유효성 검증 실패 |

### Board Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-BOARD-001 | unit | 정상 요청으로 게시글 작성 | 성공, BoardResponse 반환 |
| T-BOARD-002 | unit | 제목이 빈값으로 게시글 작성 | 400 유효성 검증 실패 |
| T-BOARD-003 | unit | 존재하는 ID로 게시글 조회 | 성공, BoardResponse 반환 |
| T-BOARD-004 | unit | 존재하지 않는 ID로 게시글 조회 | 404 예외 발생 |
| T-BOARD-005 | unit | 게시글 전체 조회 | 성공, List 반환 |
