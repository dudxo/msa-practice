# 설계 문서: JWT 인증 — Gateway 통합

## 1. 메타 정보

- 작성일: 2026-04-10
- 도메인: User (로그인, JWT 발급), Gateway (JWT 검증)
- 관련: MSA 학습 커리큘럼 섹션 9

---

## 2. 배경

- 각 서비스가 인증 로직을 중복 구현하면 유지보수 부담 증가
- MSA는 Stateless 원칙 → 세션 대신 JWT 선택
- Gateway에서 JWT 검증 + X-User-Id 헤더로 전파 → 서비스는 헤더만 신뢰
- JWT는 암호화가 아닌 서명 → 개인정보(이름/이메일)는 담지 않고 userId만 포함

---

## 3. To-do 리스트

### User Service
- [ ] 1. build.gradle에 JJWT 의존성 추가
  - 영향 파일: `user-service/build.gradle`
- [ ] 2. User 엔티티에 password 필드 추가
  - 영향 파일: `user-service/src/main/java/com/msa/user/entity/User.java`
- [ ] 3. CreateUserRequest에 password 필드 추가
  - 영향 파일: `user-service/src/main/java/com/msa/user/dto/CreateUserRequest.java`
- [ ] 4. LoginRequest, LoginResponse DTO 생성
  - 영향 파일: `user-service/src/main/java/com/msa/user/dto/Login*.java`
- [ ] 5. UserRepository에 findByEmail 추가
  - 영향 파일: `user-service/src/main/java/com/msa/user/repository/UserRepository.java`
- [ ] 6. JwtUtil 생성 (generateToken, validateToken, extractUserId)
  - 영향 파일: `user-service/src/main/java/com/msa/user/util/JwtUtil.java`
- [ ] 7. UserService에 login 메서드 추가
- [ ] 8. UserController에 POST /users/login 엔드포인트 추가

### Gateway Service
- [ ] 9. build.gradle에 JJWT 의존성 추가
  - 영향 파일: `gateway-service/build.gradle`
- [ ] 10. JwtUtil 생성 (User Service와 같은 비밀키로 검증)
- [ ] 11. JwtAuthFilter 구현 (order: 0)
  - 공개 API: POST /users, POST /users/login, GET /boards, GET /boards/*
  - 나머지는 Bearer 토큰 검증 필수
  - 검증 성공 시 기존 X-User-Id 헤더 제거 후 검증된 값으로 교체 (위조 방지)

---

## 4. API 설계서

### 신규 엔드포인트

**POST /users/login**
```json
// 요청
{ "email": "hong@test.com", "password": "password123" }

// 응답
{ "token": "eyJhbGci...", "userId": 1, "name": "홍길동" }
```

### 공개/보호 API 구분
| 메서드 | 경로 | 인증 |
|---|---|---|
| POST | /users | X |
| POST | /users/login | X |
| GET | /users/{id} | O |
| POST | /boards | O |
| GET | /boards | X |
| GET | /boards/{id} | X |

### 서비스 간 전달 헤더
| 헤더 | 설명 |
|---|---|
| X-User-Id | Gateway가 JWT에서 추출해 전파 |

---

## 5. DB 설계서

### users 테이블 변경
```sql
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL;
```

---

## 6. 테스트 관점

### User Service
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-USER-011 | unit | 올바른 email/password로 로그인 | JWT 반환 |
| T-USER-012 | unit | 존재하지 않는 email | IllegalArgumentException |
| T-USER-013 | unit | 틀린 password | IllegalArgumentException |
| T-JWT-001 | unit | 토큰 생성 후 userId 추출 | 일치 |
| T-JWT-002 | unit | 유효 토큰 validateToken | true |
| T-JWT-003 | unit | 잘못된 토큰 validateToken | false |

### Gateway
| ID | 유형 | 시나리오 | 기대 결과 |
|---|------|---------|----------|
| T-GW-006 | integration | 토큰 없이 보호 API 호출 | 401 |
| T-GW-007 | integration | 잘못된 토큰 | 401 |
| T-GW-008 | integration | 유효 토큰 | 필터 통과 |
| T-GW-009 | integration | 공개 API (회원가입) | 토큰 없이 통과 |
| T-GW-010 | integration | 공개 API (로그인) | 토큰 없이 통과 |
| T-GW-011 | integration | 공개 API (게시글 목록) | 토큰 없이 통과 |

---

## 7. 보안 고려사항

1. **JWT 페이로드 최소화**: userId만 포함, 개인정보 제외
2. **헤더 위조 방지**: Gateway에서 기존 X-User-Id 헤더 제거 후 검증된 값으로 교체
3. **네트워크 격리**: 실무에서는 Gateway만 외부 공개, 나머지 서비스는 내부 네트워크
4. **에러 메시지 통일**: "이메일 없음"/"비밀번호 틀림" 구분 안 함 → 이메일 존재 여부 노출 방지
