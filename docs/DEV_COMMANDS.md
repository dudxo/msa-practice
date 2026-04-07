# 개발 커맨드 정리

## 인프라 (Docker Compose)

```bash
# 전체 인프라 실행 (MySQL 등)
docker compose up -d

# 전체 인프라 중지
docker compose down

# 로그 확인
docker compose logs -f user-db
docker compose logs -f board-db
```

## 테스트

```bash
# User Service 테스트
cd user-service && ./gradlew test

# Board Service 테스트
cd board-service && ./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.msa.user.service.UserServiceTest"

# 특정 테스트 메서드만 실행
./gradlew test --tests "com.msa.user.service.UserServiceTest.createUser_success"
```

## 개발 서버

```bash
# User Service 실행 (포트 8081)
cd user-service && ./gradlew bootRun

# Board Service 실행 (포트 8082)
cd board-service && ./gradlew bootRun
```

## 빌드

```bash
# 빌드
cd user-service && ./gradlew build
cd board-service && ./gradlew build

# 테스트 스킵 빌드
./gradlew build -x test
```

## 데이터베이스

```bash
# User DB 접속 (포트 3321)
docker exec -it user-db mysql -u root -proot user_db

# Board DB 접속 (포트 3322)
docker exec -it board-db mysql -u root -proot board_db
```
