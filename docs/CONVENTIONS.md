# CONVENTIONS.md

## API 응답 포맷

### 성공 응답
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@test.com",
  "createdAt": "2026-04-07T12:00:00"
}
```

### 에러 응답
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "이메일은 필수입니다."
}
```

### 목록 응답
```json
[
  { "id": 1, "title": "첫 번째 게시글" },
  { "id": 2, "title": "두 번째 게시글" }
]
```

---

## 에러 코드 규칙

| HTTP 상태 | 사용 상황 |
|----------|----------|
| 200 | 조회 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 (유효성 검증 실패) |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 충돌 (중복 데이터 등) |
| 500 | 서버 내부 오류 |

---

## 네이밍 규칙

### 패키지 구조 (서비스별)
```
com.msa.{서비스명}/
├── controller/     # REST API 엔드포인트
├── service/        # 비즈니스 로직
├── repository/     # 데이터 접근
├── entity/         # JPA 엔티티
└── dto/            # 요청/응답 DTO
```

### 클래스 네이밍
| 유형 | 네이밍 패턴 | 예시 |
|-----|-----------|------|
| 엔티티 | `{도메인}` | `User`, `Board` |
| 컨트롤러 | `{도메인}Controller` | `UserController` |
| 서비스 | `{도메인}Service` | `UserService` |
| 리포지토리 | `{도메인}Repository` | `UserRepository` |
| 요청 DTO | `Create{도메인}Request` | `CreateUserRequest` |
| 응답 DTO | `{도메인}Response` | `UserResponse` |
| 테스트 | `{대상}Test` | `UserServiceTest` |

### API 경로
- 복수형 명사: `/users`, `/boards`
- 서비스 간 내부 API: `/internal/users/{id}`
- 외부 API: `/users`, `/boards`
