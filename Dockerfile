# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Point to the Gateway's pom.xml from the repo root
COPY "Software Developers/Banking API/Gateway/pom.xml" .
RUN mvn dependency:go-offline

COPY "Software Developers/Banking API/Gateway/src" "./src"
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Gateway usually runs on 8080 or 9090
EXPOSE 8080

# Use $PORT for Render and include your JWT Secret
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-Xmx512m", "-jar", "app.jar"]