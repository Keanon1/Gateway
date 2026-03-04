# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Since pom.xml is in the same folder as the Dockerfile, use simple paths
COPY ["pom.xml", "."]
RUN mvn dependency:go-offline

COPY ["src", "./src"]
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Run with Render's Dynamic Port and Memory Limit
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-Xmx512m", "-jar", "app.jar"]
