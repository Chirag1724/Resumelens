# --- Build Stage ---
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper and POM first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and package executable jar
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# --- Production Runtime Stage ---
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Security: Create and run under non-root system user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy compiled jar from build stage
COPY --from=builder --chown=spring:spring /app/target/resumelens-backend-0.0.1-SNAPSHOT.jar app.jar

# JVM optimization flags for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
