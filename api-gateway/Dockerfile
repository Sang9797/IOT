FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy the built JAR file
COPY api-gateway/target/api-gateway-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
