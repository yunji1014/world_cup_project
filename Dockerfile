FROM ubuntu:latest
LABEL authors="1014y"

ENTRYPOINT ["top", "-b"]

# --- 1. Build Stage ---
# Java 24 JDK가 포함된 베이스 이미지 사용
# (만약 eclipse-temurin:24-jdk가 아직 없다면 openjdk:24-slim 으로 변경 가능)
FROM openjdk:24-jdk-slim AS builder

WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 부여 (필수)
RUN chmod +x gradlew

# 의존성 다운로드 (빌드 속도 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew build --no-daemon -x test

# --- 2. Run Stage ---
# 실행을 위한 Java 24 베이스 이미지
FROM openjdk:24-jdk-slim

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]