---
name: implement
description: TDD Green 단계. 테스트를 통과하는 최소 코드 구현.
disable-model-invocation: true
---

# Implement (TDD Green 단계)

테스트를 통과하는 구현 코드를 작성한다.
**최소한의 코드로 테스트 통과 (YAGNI)**

## 입력

- 테스트 설계자가 작성한 **테스트 코드** (현재 실패 상태)
- To-do 리스트 (참고용)

## 출력

- 구현 코드 (테스트 통과 상태)
- **Swagger 데코레이터** (Controller 작성 시 함께)
- 필요시 리팩터링

## 필수 읽기 문서

| 상황 | 문서 |
|-----|-----|
| 항상 | `docs/CONVENTIONS.md` - 코딩 규약 |
| 항상 | `docs/DEV_COMMANDS.md` - 테스트/빌드 명령어 |
| 항상 | 테스트 코드 - 기대 동작 파악 |
| 필요시 | 기존 관련 코드 - 패턴/스타일 파악 |

## 행동 원칙

### DO
- 테스트 통과가 최우선 목표 (TDD - Green 단계)
- `docs/CONVENTIONS.md` 규약 철저히 준수
  - API 응답: HTTP Status Code 활용, wrapper 없이 직접 반환
  - 에러 코드: `{DOMAIN}_{상황}` 형태 (예: `ORDER_NOT_CANCELABLE`)
  - 네이밍: 기존 패턴 따르기
- 기존 코드 스타일/패턴 유지
- 작은 단위로 커밋

### DON'T
- 테스트 없는 코드 작성
- 과도한 추상화/일반화
- 요청받지 않은 기능 추가 (과잉 엔지니어링)
- CONVENTIONS 위반

## 구현 패턴 예시

### Swagger 데코레이터 (Controller 작성 시 필수)

```typescript
// src/orders/orders.controller.ts

@Post('cancel')
@ApiOperation({
  summary: '주문 취소',
  description: '일반 주문은 PAID 상태만 취소 가능. 예약 주문은 배송 전이면 취소 가능.'
})
@ApiResponse({ status: 200, description: '취소 성공' })
@ApiResponse({ status: 409, description: 'ORDER_NOT_CANCELABLE - 취소 불가 상태' })
async cancelOrder(@Body() dto: CancelOrderDto) {
  return this.ordersService.cancelOrder(dto.orderId);
}
```

### 에러 처리 (CONVENTIONS 준수)

```typescript
// Bad - wrapper 사용
return {
  success: false,
  error: { code: 'ORDER_NOT_FOUND', message: '...' }
};

// Good - HTTP Status + 직접 throw
throw new NotFoundException({
  code: 'ORDER_NOT_FOUND',
  message: '주문을 찾을 수 없습니다.',
});
```

### 비즈니스 로직 분기

```typescript
// src/orders/orders.service.ts

async cancelOrder(orderId: string): Promise<void> {
  const order = await this.findOneOrFail(orderId);

  // 예약 주문 vs 일반 주문 분기
  if (order.reservedAt) {
    if (order.status >= OrderStatus.SHIPPED) {
      throw new ConflictException({
        code: 'ORDER_NOT_CANCELABLE',
        message: '배송이 시작된 예약 주문은 취소할 수 없습니다.',
      });
    }
  } else {
    if (order.status !== OrderStatus.PAID) {
      throw new ConflictException({
        code: 'ORDER_NOT_CANCELABLE',
        message: '이 주문은 취소할 수 없는 상태입니다.',
      });
    }
  }

  await this.processCancel(order);
}
```

## TDD 사이클

```
1. Red   → 테스트 실패 확인 (Test가 완료)
2. Green → 테스트 통과하는 최소 코드 작성 ← 지금 여기!
3. Refactor → 코드 정리 (테스트 유지)
```

## 완료 체크리스트

- [ ] 모든 테스트 통과 (`npm test`)
- [ ] `docs/CONVENTIONS.md` 규약 준수
- [ ] Swagger 데코레이터 작성
- [ ] 린트 에러 없음 (`npm run lint`)
- [ ] 타입 에러 없음 (`npm run build`)
