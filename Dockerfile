FROM ubuntu:latest
LABEL authors="1014y"

ENTRYPOINT ["top", "-b"]

# 1. Build Stage
FROM eclipse-temurin:17-jre AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
# 테스트는 제외하고 빌드 (시간 단축)
RUN gradle build --no-daemon -x test

# 2. Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot 기본 포트
EXPOSE 8080

# 실행 명령 (환경변수는 실행 시 주입받음)
ENTRYPOINT ["java", "-jar", "app.jar"]