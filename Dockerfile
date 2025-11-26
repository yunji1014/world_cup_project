FROM ubuntu:latest
LABEL authors="1014y"

ENTRYPOINT ["top", "-b"]

# 1. Build Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
# 테스트 제외하고 빌드
RUN gradle build --no-daemon -x test

# 2. Run Stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]