# syntax=docker/dockerfile:1

# ---------- Stage 1: build the JAR with Maven + JDK ----------
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only the POM first and download dependencies. This layer is cached and
# re-used as long as pom.xml does not change, so code-only changes build fast.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy the sources and build. Tests are run by the Jenkins Test stage, not here.
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: minimal runtime image (JRE only, no Maven/JDK/sources) ----------
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="calculator" \
      org.opencontainers.image.description="Calculator REST API (Spring Boot)" \
      org.opencontainers.image.source="https://github.com/RAMYA-PARSANIA/spe_mini_project"

# Run as an unprivileged user instead of root
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app

COPY --from=build --chown=app:app /workspace/target/calculator.jar app.jar

USER app

# Size the heap relative to the container's memory limit
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# busybox wget ships with Alpine, so no extra packages are needed for the probe
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
