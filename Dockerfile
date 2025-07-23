# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set environment variable
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="" \
    PORT=8080

# Expose port
EXPOSE 8080

# Add a volume pointing to /tmp
VOLUME /tmp

# Copy the application jar to the container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
