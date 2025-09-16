# 빌드 스테이지
FROM gradle:8.14-jdk17 AS builder
WORKDIR /app

# 캐시를 위해 wrapper 먼저 복사
COPY gradlew ./
COPY gradle/wrapper/ ./gradle/wrapper/

# 나머지 소스 복사
COPY . .

# 윈도우 줄바꿈 제거 + 실행 권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# --no-daemon 권장
RUN ./gradlew clean build -x test --no-daemon

# 실행 스테이지
FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
