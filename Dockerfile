FROM ubuntu:latest
LABEL authors="1014y"

ENTRYPOINT ["top", "-b"]

# 1. Build Stage
# Gradle 이미지가 아닌, 순수 JDK 이미지를 사용합니다.
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Gradle Wrapper 실행을 위한 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 부여 (매우 중요)
RUN chmod +x gradlew

# 의존성 다운로드 (캐싱 활용을 위해 소스 복사 전 실행)
RUN ./gradlew dependencies --no-daemon

# 소스 복사 및 빌드 실행
COPY src ./src
# 'gradle' 대신 './gradlew'를 사용합니다.
RUN ./gradlew build --no-daemon -x test

# 2. Run Stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]