# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Give execute permission to gradlew
RUN chmod +x ./gradlew

# Build the application with Java 17
RUN ./gradlew build -x test

# Debug: Check what files were generated
RUN echo "=== Checking build directory ==="
RUN ls -la build/
RUN echo "=== Checking build/libs directory ==="
RUN ls -la build/libs/ || echo "build/libs directory not found"
RUN echo "=== Finding all JAR files ==="
RUN find . -name "*.jar" -type f
RUN echo "=== Finding all WAR files ==="
RUN find . -name "*.war" -type f

# Copy the built JAR file
RUN cp build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"] 