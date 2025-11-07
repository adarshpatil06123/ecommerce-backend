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
COPY common common
COPY payment-service payment-service
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl payment-service -am package

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl netcat-openbsd bash
COPY --from=builder /build/payment-service/target/*.jar app.jar
COPY wait-for.sh ./wait-for.sh
RUN sed -i 's/\r$//' wait-for.sh && chmod +x wait-for.sh
EXPOSE 8084
ENTRYPOINT ["./wait-for.sh","mysql-payment","java","-jar","/app/app.jar"]
