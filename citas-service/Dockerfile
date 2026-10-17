# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system spring && useradd --system --gid spring --home-dir /app spring
WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/target/*.jar /app/app.jar

USER spring:spring
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

