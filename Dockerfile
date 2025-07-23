# === Stage 1: Build the JAR ===
FROM maven:3.9.6-eclipse-temurin-17 as build

WORKDIR /app

# Copy only necessary files first for better caching
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# === Stage 2: Run the app ===
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
