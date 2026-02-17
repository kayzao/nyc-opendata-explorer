# ---- 1) Build stage: compile the Spring Boot jar ----
FROM gradle:8.10-jdk21 AS build
WORKDIR /app

# Copy everything into the build image
COPY . .

# Build the Spring Boot fat jar
RUN gradle bootJar --no-daemon

# ---- 2) Run stage: lightweight JRE to run the jar ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# The app listens on 8080 by default
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

