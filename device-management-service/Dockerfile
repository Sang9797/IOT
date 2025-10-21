FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy the built JAR file
COPY device-management-service/target/device-management-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
