FROM --platform=linux/amd64 gradle:9.2.1-jdk17 AS builder

WORKDIR /home/gradle/project
COPY . .

# gradle 빌드 (test 제외)
RUN gradle clean build -x test --no-daemon

FROM --platform=linux/amd64 eclipse-temurin:17-jre-jammy

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
