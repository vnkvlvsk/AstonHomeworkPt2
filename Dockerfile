# Single shared image for all 5 Spring Boot apps in this module (Homework4Application,
# Homework5Application, DiscoveryServerApplication, ConfigServerApplication,
# ApiGatewayApplication). They already coexist on one classpath locally (each scans only
# its own package), so one image + a MAIN_CLASS env var per container is simpler and more
# honest than pretending this is a multi-module build. See docker-compose.yml.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependency resolution in its own layer, invalidated only when pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# .dockerignore strips the gitignored local *.properties (real secrets); every app gets
# its runtime config from docker-compose environment variables instead.
COPY src ./src
RUN mvn -B -q -DskipTests compile \
 && mvn -B -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=/build/target/lib

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# curl for docker-compose healthchecks against each app's /actuator/health.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/lib ./lib

# Set per-service in docker-compose.yml, e.g. Homework4.Homework4Application.
ENV MAIN_CLASS=""
ENTRYPOINT ["sh", "-c", "exec java -cp classes:lib/* ${MAIN_CLASS}"]
