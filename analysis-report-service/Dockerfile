FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy the built JAR file
COPY analysis-report-service/target/analysis-report-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
