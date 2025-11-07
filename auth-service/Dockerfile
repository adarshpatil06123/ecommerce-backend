# ---- Stage 1: Build ----
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build
# Copy root and module POMs first for dependency caching
COPY pom.xml ./
# Copy all module POMs (Maven needs all to resolve reactor)
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
# Pre-fetch dependencies
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -e -U dependency:go-offline
# Copy sources needed to build this module (and its deps)
COPY common common
COPY auth-service auth-service
# Build only this module and required modules
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl auth-service -am package

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Install minimal tools for healthchecks and wait script
RUN apk add --no-cache curl netcat-openbsd bash
# Copy built jar
COPY --from=builder /build/auth-service/target/*.jar app.jar
# Copy wait script from repo root (compose build context is project root)
COPY wait-for.sh ./wait-for.sh
# Normalize line endings in case the repo is checked out on Windows
RUN sed -i 's/\r$//' wait-for.sh && chmod +x wait-for.sh
EXPOSE 8081
ENTRYPOINT ["./wait-for.sh","mysql-auth","java","-jar","/app/app.jar"]
