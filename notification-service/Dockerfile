FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy the built JAR file
COPY notification-service/target/notification-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
