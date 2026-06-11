# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -DskipTests

COPY src src
RUN ./mvnw -B package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r scm && useradd -r -g scm scm \
    && mkdir -p /app/uploads && chown -R scm:scm /app

COPY --from=build /app/target/*.jar app.jar
RUN chown scm:scm app.jar

USER scm

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8085 \
    UPLOAD_DIR=/app/uploads \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD sh -c 'curl -fsS "http://127.0.0.1:$${PORT:-$${SERVER_PORT:-8085}}/actuator/health" || exit 1'

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
