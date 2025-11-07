# ---- Stage 1: Build ----
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml ./
# Copy all module POMs (Maven needs all to resolve reactor)
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -e -U dependency:go-offline
# Copy only the modules we need to build
COPY common common
COPY gateway-service gateway-service
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl gateway-service -am package

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl netcat-openbsd
COPY --from=builder /build/gateway-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
