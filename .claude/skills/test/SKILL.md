---
name: test
description: TDD Red 단계. 실패하는 테스트 코드 작성. 단위 테스트와 E2E 테스트 포함.
disable-model-invocation: true
---

# Test (TDD Red 단계)

테스트 코드를 작성한다. **구현 코드보다 테스트를 먼저 작성한다.**

## 입력

- 플래너가 작성한 **To-do 리스트**
- 아키텍트의 **API 설계서** (있다면)

## 출력

- 테스트 코드 파일
  - Unit test: `*ServiceTest.java`, `*ControllerTest.java`
  - Integration test: `*IntegrationTest.java` (필요시)
- 테스트 케이스 요약

## 필수 읽기 문서

| 상황 | 문서 |
|-----|-----|
| 항상 | To-do 리스트 |
| 항상 | `docs/DEV_COMMANDS.md` - 테스트/빌드 명령어 |
| 기존 기능 수정 시 | `docs/DOMAIN.md` - 서비스 개요 및 도메인 파악 |
| 기존 기능 수정 시 | `docs/features/{도메인}/feature.md` - 현재 기능 상세 파악 |
| 기존 기능 수정 시 | 기존 테스트 파일 (`*Test.java`) |

## 행동 원칙

### DO
- 테스트 먼저 작성 (TDD - Red 단계)
- Happy path + Edge case + Error case 모두 커버
- 테스트 설명은 한글로 명확하게 (`it('예약 주문은 배송 전 취소 가능해야 한다')`)
- 기존 테스트 패턴/스타일 따르기
- 테스트 ID 부여 권장 (T-{기능}-001)

### DON'T
- 구현 코드 작성 (테스트만!)
- 기존 테스트 임의로 삭제
- 테스트 없이 다음 단계 진행

## Spring Boot 테스트 구조 예시

```java
// src/test/java/com/msa/user/service/UserServiceTest.java

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 - 정상적인 요청이면 사용자가 생성되어야 한다")
    void createUser_success() {
        // given
        CreateUserRequest request = new CreateUserRequest("testUser", "test@test.com");
        given(userRepository.save(any())).willReturn(new User(1L, "testUser", "test@test.com"));

        // when
        UserResponse response = userService.createUser(request);

        // then
        assertThat(response.getName()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("회원가입 - 이메일이 중복이면 예외가 발생해야 한다")
    void createUser_duplicateEmail_throwsException() {
        // given
        given(userRepository.existsByEmail(any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

## 테스트 케이스 요약 형식

> **모든 테스트 케이스가 요약에 포함되어야 합니다!**

```markdown
## 테스트 케이스 요약

### 기존 테스트
| ID | 유형 | 시나리오 | 기대 결과 |
|----|-----|---------|----------|
| T-CANCEL-001 | unit | 일반 주문, PAID → 취소 | 성공 |
| T-CANCEL-002 | unit | 일반 주문, SHIPPED → 취소 | 실패 |

### 신규 테스트
| ID | 유형 | 시나리오 | 기대 결과 |
|----|-----|---------|----------|
| T-CANCEL-003 | unit | 예약 주문, 배송 전 → 취소 | 성공 |
| T-CANCEL-004 | unit | 예약 주문, 배송 후 → 취소 | 실패 |

### 총계
- 기존: 2개
- 신규: 2개
- **전체: 4개**

현재 상태: Red (테스트 실패)
```

## 검증

테스트 작성 후 `./gradlew test`를 실행하여 **테스트가 실패하는지 확인**한다.
(아직 구현이 없으므로 실패해야 정상)

각 서비스별로 테스트 실행:
```bash
cd user-service && ./gradlew test
cd board-service && ./gradlew test
```

실패 결과를 사용자에게 보고한다.
