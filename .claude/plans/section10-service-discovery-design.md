# 설계 문서: Service Discovery — Eureka

## 1. 메타 정보

- 작성일: 2026-04-13
- 도메인: 전체 서비스 (Eureka 등록/조회)
- 관련: MSA 학습 커리큘럼 섹션 10

---

## 2. 배경

- 서비스 주소가 하드코딩(`http://localhost:8081`)되어 있어 확장/배포 시 재설정 필요
- 서비스 인스턴스가 늘어날 때 로드밸런싱을 Gateway에 수동 구성 필요
- Eureka Server로 Service Registry를 구축하고, 각 서비스는 시작 시 자동 등록
- Gateway와 RestTemplate에 `@LoadBalanced`/`lb://` 적용 → Client-Side Load Balancing

---

## 3. To-do 리스트

### 프로젝트 세팅
- [ ] 1. discovery-service 프로젝트 생성 (Eureka Server)
  - 영향 파일: `discovery-service/build.gradle`
- [ ] 2. @EnableEurekaServer 적용
  - 영향 파일: `discovery-service/src/main/java/com/msa/discovery/DiscoveryServiceApplication.java`
- [ ] 3. application.yml 설정 (포트 8761, register-with-eureka: false, fetch-registry: false)
  - 영향 파일: `discovery-service/src/main/resources/application.yml`

### 각 서비스 Eureka Client 등록
- [ ] 4. 4개 서비스 build.gradle에 `spring-cloud-starter-netflix-eureka-client` 추가
  - 영향 파일: user/board/point/gateway-service/build.gradle
- [ ] 5. 각 서비스 application.yml에 Eureka 설정 추가 (defaultZone, prefer-ip-address)

### Gateway 라우팅 변경
- [ ] 6. uri를 `http://localhost:808x` → `lb://service-name`으로 변경
  - 영향 파일: `gateway-service/src/main/resources/application.yml`

### Board/User Service RestTemplate
- [ ] 7. RestTemplateConfig에 `@LoadBalanced` 추가
  - 영향 파일: 각 서비스 `config/RestTemplateConfig.java`
- [ ] 8. 서비스 호출 URL을 `http://user-service`, `http://point-service`로 변경
  - 영향 파일: `PointServiceClient.java`, 각 application.yml

### 테스트
- [ ] 9. UserServiceApplicationTests @Disabled (Eureka/DB 연결 필요한 통합 테스트)

---

## 4. API 설계서

API 변경 없음. 내부 통신 주소 해석 방식만 바뀜.

---

## 5. DB 설계서

스키마 변경 없음.

---

## 6. 테스트 관점

### 수동 검증 (통합)
| 시나리오 | 기대 결과 |
|---|---|
| Eureka 대시보드 (8761) 접근 | USER/BOARD/POINT/GATEWAY 4개 서비스 UP 표시 |
| Gateway(8080)로 /boards GET | Board Service가 UserInfo 조회까지 체인 호출 성공 |
| Eureka `/eureka/apps` API | 각 서비스 instance 정보 반환 |

### 단위 테스트
기존 테스트 유지. Eureka 관련 설정은 Spring Cloud Starter가 자동 주입.

---

## 7. 변경 전후 비교

### Before
```yaml
# Gateway
uri: http://localhost:8081   # 하드코딩

# Board Service
user-service.url: http://localhost:8081
point-service.url: http://localhost:8083
```

### After
```yaml
# Gateway
uri: lb://user-service       # Eureka 자동 조회 + LB

# Board Service
user-service.url: http://user-service
point-service.url: http://point-service
```

모든 서비스가 시작할 때 Eureka에 자동 등록, 30초마다 하트비트 전송.
