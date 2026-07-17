# syntax=docker/dockerfile:1.7
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY study-management-api/pom.xml study-management-api/pom.xml
COPY study-management-common/pom.xml study-management-common/pom.xml
COPY study-management-domain/pom.xml study-management-domain/pom.xml
COPY study-management-manager/pom.xml study-management-manager/pom.xml
COPY study-management-repository/pom.xml study-management-repository/pom.xml
COPY study-management-service/pom.xml study-management-service/pom.xml
COPY study-management-test/pom.xml study-management-test/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline
COPY study-management-api ./study-management-api
COPY study-management-common ./study-management-common
COPY study-management-domain ./study-management-domain
COPY study-management-manager ./study-management-manager
COPY study-management-repository ./study-management-repository
COPY study-management-service ./study-management-service
COPY study-management-test ./study-management-test
COPY frontend ./frontend
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp verify

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/study-management-service/target/study-management-service-*-exec.jar app.jar
USER 10001
EXPOSE 8080 9090
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Djava.security.egd=file:/dev/urandom","-jar","/app/app.jar"]
