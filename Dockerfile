# ---------- Stage 1: Build ----------
FROM maven:3.9.4-eclipse-temurin-21-alpine AS build

# Set workdir
WORKDIR /app

# Copy the pom and download dependencies (so cache is used intelligently)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the app
RUN mvn clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jdk-alpine

# Set workdir in runtime container
WORKDIR /app

# Copy the fat jar from the build container
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
