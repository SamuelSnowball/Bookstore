# Build stage - multi-module Maven project
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom and all module poms for dependency resolution
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth_service/pom.xml auth_service/pom.xml
COPY entity_service/pom.xml entity_service/pom.xml
COPY order_service/pom.xml order_service/pom.xml
COPY payment_service/pom.xml payment_service/pom.xml

# Download dependencies for better caching
RUN mvn dependency:go-offline -B -pl auth_service -am

# Copy source code for common and auth_service
COPY common/src common/src
COPY auth_service/src auth_service/src

# Build the application (skip Liquibase and jOOQ - generated code already in src)
RUN mvn clean package -DskipTests -Dpmd.skip=true -Dliquibase.skip=true -Djooq.codegen.skip=true -pl auth_service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar file (original, not -exec)
COPY --from=build /workspace/auth_service/target/auth-service-*.jar app.jar

# Expose port
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9000/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
