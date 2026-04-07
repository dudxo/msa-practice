# Project Rules

이 프로젝트는 **TDD 개발 프로세스**를 따르며, Skill + Agent 하이브리드 구조로 워크플로우를 관리합니다.

> **시작하기 전에**
> 이 하네스를 프로젝트에 적용한 뒤, 아래 문서를 프로젝트에 맞게 작성하세요:
> - [`docs/DOMAIN.md`](docs/DOMAIN.md) — 서비스 개요, 핵심 도메인, 용어 정의, 아키텍처, 비즈니스 규칙
> - [`docs/CONVENTIONS.md`](docs/CONVENTIONS.md) — API 응답 포맷, 에러 코드, 네이밍 규칙
> - [`docs/DEV_COMMANDS.md`](docs/DEV_COMMANDS.md) — 테스트/빌드/개발 서버 명령어
>
> 기존 코드베이스가 있다면 `claude --agent legacy-doc`으로 자동 문서화를 시작할 수 있습니다.

---

## 워크플로우

개발은 **2-Phase**로 진행됩니다.

```
Phase 1 (설계): /plan [기능] → Q&A → To-do → API/DB 설계 → 사용자 컨펌
Phase 2 (구현): /develop [설계문서] → Test → Implement → Review → 노션 정리 + 커밋
                                       ↑ 각 단계는 Agent에게 위임 (별도 컨텍스트)
```

1. `/plan [기능 설명]`으로 설계 문서(To-do + API/DB 설계)를 작성합니다.
2. 사용자 컨펌을 받습니다.
3. `/develop [설계문서 경로 or 링크]`로 구현을 시작합니다.

---

## 아키텍처: Skill + Agent 하이브리드

| 유형 | 역할 | 실행 컨텍스트 |
|-----|------|-------------|
| **Skill** (대화형) | 사용자 상호작용, 워크플로우 오케스트레이션 | 메인 컨텍스트 |
| **Agent** (실행자) | 실제 작업 수행 (테스트, 구현, 리뷰 등) | 별도 컨텍스트 |
| **Skill** (리소스) | 템플릿, 가이드 보관 (자동 실행 안됨) | - |

### Skills (대화형)

| 역할 | Skill | 호출 | 언제 사용? |
|-----|-------|------|----------|
| 플래너 | `plan` | `/plan [설명]` | 기획 분석, To-do + API/DB 설계 |
| 구현 오케스트레이터 | `develop` | `/develop [설계문서]` | 설계 문서 기반 구현 워크플로우 |
| 아키텍트 | `design` | `/design` | API/DB 설계 (독립 사용) |

### Agents (실행자)

| 역할 | Agent | 호출 | 언제 사용? |
|-----|-------|------|----------|
| 테스트 설계자 | `test` | `claude --agent test` | TDD 테스트 코드 작성 |
| 개발자 | `implement` | `claude --agent implement` | 구현 코드 작성 |
| 코드 리뷰어 | `review` | `claude --agent review` | 코드 품질 검토 |

### 사용 예시
```
사용자: /plan User Service 회원가입 기능            → 설계 문서 작성 (Skill)
사용자: /develop .claude/plans/user-signup-design.md → 설계 기반 구현 시작 (Skill → Agent 위임)
사용자: /develop                                     → 설계 문서 경로를 질문함
```

---

## 문서 템플릿

| 문서 | 위치 |
|-----|-------|
| 설계 문서 | `.claude/skills/plan/templates/design.md` |

---

## 세션(섹션) 완료 시 규칙

각 섹션이 끝나면 반드시 아래 순서를 따른다:

1. **커밋** — 해당 섹션에서 변경된 코드를 커밋
   - 커밋 메시지 형식: `섹션 N: [섹션 제목]`
   - 예: `섹션 2: 기본 마이크로서비스 구축 (User + Board Service)`
2. **노션 정리** — 섹션 상세 페이지 생성 (Part 1: 개념, Part 2: Q&A, Part 3: 면접)
3. **커리큘럼 업데이트** — 노션 커리큘럼 페이지에서 완료 표시 (✅)

> 커밋은 사용자가 요청할 때만 수행한다. 자동으로 커밋하지 않는다.

---

## 핵심 규칙

1. **플래너 필수** - 개발 시작 전 `/plan`으로 To-do 작성 + 사용자 컨펌
2. **질문 필수** - 플래너는 모호한 요구사항에 대해 질문 필수
3. **TDD 원칙** - 테스트 먼저, 코드는 나중
4. **CONVENTIONS 준수** - `docs/CONVENTIONS.md`의 API 응답, 에러 코드, 네이밍 규칙 준수
5. **순서 준수** - 워크플로우 순서대로 진행 (건너뛰기 금지)

---

## 기술 스택

- Backend: Java 17 + Spring Boot 3.5.x (멀티 모듈 MSA)
- DB: Spring Data JPA + MySQL 8.0
- 테스트: JUnit 5 + MockMvc + Testcontainers
- 빌드: Gradle (Groovy)
- 인프라: Docker Compose
- 메시징: Apache Kafka (추후 도입)

## 프로젝트 구조

```
msa/
├── docker-compose.yml
├── user-service/       (포트 8081)
├── board-service/      (포트 8082)
├── point-service/      (포트 8083, 추후)
├── gateway-service/    (추후)
└── discovery-service/  (추후)
```
