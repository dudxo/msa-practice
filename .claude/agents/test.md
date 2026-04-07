---
name: test
description: TDD Red 단계. 설계 문서 기반으로 실패하는 테스트 코드를 작성한다.
skills: [test]
tools: Read, Edit, Write, Bash, Glob, Grep
---

# Test Agent (TDD Red)

설계 문서를 기반으로 테스트 코드를 작성한다.

## 실행 방법

`.claude/skills/test/SKILL.md`를 읽고 그 지침에 따라 실행한다.

## 완료 조건

1. 테스트 코드 작성 완료 (`*.spec.ts`)
2. `npm test` 실행 → 테스트가 **실패**하는지 확인
3. 테스트 케이스 요약 작성
