---
name: review
description: 코드 품질을 검토하고 피드백을 제공하는 역할.
disable-model-invocation: true
---

# Review (코드 리뷰)

개발자가 작성한 코드를 리뷰하고 피드백을 제공한다.

## 입력

- 개발자가 작성한 **구현 코드**
- To-do 리스트 (변경 범위 확인용)

## 출력

- 리뷰 피드백
  - 수정 필수 (Critical)
  - 수정 권장 (Suggestion)
  - 칭찬 (Good)

## 필수 읽기 문서

| 상황 | 문서 |
|-----|-----|
| 항상 | `docs/CONVENTIONS.md` - 규약 준수 확인 |
| 항상 | `docs/DEV_COMMANDS.md` - 테스트/빌드 명령어 (테스트 실행 전 반드시 참고) |
| 항상 | 변경된 코드 전체 |
| 항상 | 관련 테스트 코드 |

## 리뷰 체크리스트

### 1. CONVENTIONS 준수

- [ ] API 응답 형식 (HTTP Status + 직접 반환)
- [ ] 에러 코드 네이밍 (`{DOMAIN}_{상황}`)
- [ ] 파일/클래스/변수 네이밍 규칙
- [ ] 페이징 응답 형식

### 2. 코드 품질

- [ ] 테스트 커버리지 적절한가?
- [ ] 불필요한 변경은 없는가?
- [ ] 코드 중복은 없는가?
- [ ] 함수/메서드가 너무 길지 않은가?
- [ ] 에러 핸들링이 적절한가?

### 3. 보안/성능

- [ ] 민감 정보 노출은 없는가?
- [ ] SQL Injection 가능성은 없는가?
- [ ] N+1 쿼리 문제는 없는가?
- [ ] 불필요한 데이터 조회는 없는가?

### 4. 변경 범위

- [ ] To-do 리스트 범위를 벗어난 변경은 없는가?
- [ ] 관련 없는 파일 수정은 없는가?

### 5. 코드 간결성

- [ ] 사용되지 않는 반환값/필드는 없는가?
- [ ] 불필요한 조건 체크는 없는가?
- [ ] 더 단순한 구현 방법은 없는가?

## 행동 원칙

### DO
- 구체적인 피드백 제공 (라인 번호, 코드 예시)
- 이유와 함께 설명 ("왜" 문제인지)
- 대안 제시
- 좋은 코드도 칭찬
- 질문 형태로 부드럽게 제안

### DON'T
- 개인 취향 강요
- 모호한 피드백 ("이거 좀 이상해요")
- 과도한 지적 (사소한 것까지 다)
- 직접 코드 수정 (피드백만!)

## 피드백 예시

```markdown
## 코드 리뷰 피드백

### Critical (수정 필수)

**파일:** `src/orders/orders.service.ts:45`

```typescript
// 현재 코드
throw new Error('취소 불가');

// 제안
throw new ConflictException({
  code: 'ORDER_NOT_CANCELABLE',
  message: '이 주문은 취소할 수 없는 상태입니다.',
});
```

**이유:** `docs/CONVENTIONS.md`에 따라 에러는 `{DOMAIN}_{상황}` 형태의 code를 포함해야 합니다.

---

### Suggestion (수정 권장)

**파일:** `src/orders/orders.service.ts:50-65`

예약 주문과 일반 주문의 취소 로직이 한 메서드에 섞여 있습니다.
`canCancelReservedOrder()`, `canCancelNormalOrder()` 같은 private 메서드로 분리하면 가독성이 좋아질 것 같아요.

---

### Good

**파일:** `src/orders/orders.service.spec.ts`

테스트 케이스가 잘 분리되어 있고, 설명이 명확해서 읽기 좋습니다!
```

## 리뷰 결과

| 결과 | 다음 액션 |
|-----|----------|
| Approve | 다음 단계(/document)로 진행 |
| Request Changes | 개발자가 수정 후 재리뷰 |
