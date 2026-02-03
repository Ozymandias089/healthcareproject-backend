# syntax=docker/dockerfile:1.5

############################
# 1) Build stage
############################
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# (권장) Gradle이 콘솔에서 깨지는 것 방지 + 빌드 옵션 기본화
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx1g"

# 1) Gradle wrapper / 설정 파일만 먼저 복사해서 의존성 캐시 레이어를 최대화
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle

# gradlew 실행권한 보장(환경에 따라 COPY 후 권한이 빠질 때가 있음)
RUN chmod +x /app/gradlew

# 2) 의존성 다운로드 레이어 (소스 바뀌어도 재사용되도록)
# - BuildKit cache mount로 Gradle 캐시를 재사용(서버에서 빌드할 때 체감 큼)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies || true

# 3) 소스 복사 후 빌드
COPY src /app/src

# (옵션) 테스트가 길면 -x test 권장. CI에서만 test 수행하는 전략이 많음.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon bootJar -x test

############################
# 2) Runtime stage
############################
FROM eclipse-temurin:21-jre
WORKDIR /app

# 운영 안정성: CA 인증서 + 타임존 데이터(메일/https 호출/시간표시 등)
RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates tzdata \
    && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Seoul

# non-root 사용자
RUN useradd -u 10001 -m -s /usr/sbin/nologin appuser

# 빌드 산출물 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 파일 소유권
RUN chown -R appuser:appuser /app

USER appuser
EXPOSE 8080

# 컨테이너 친화 JVM 옵션
# - MaxRAMPercentage는 인스턴스/컨테이너 메모리에 맞춰 자동으로 힙이 잡히게 해줌
# - ExitOnOutOfMemoryError로 OOM 시 즉시 종료 -> orchestrator(도커)가 재시작하게
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=65.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
