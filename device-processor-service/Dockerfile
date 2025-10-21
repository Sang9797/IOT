FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy the built JAR file
COPY device-processor-service/target/device-processor-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
