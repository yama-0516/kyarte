# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Build the application
RUN ./gradlew build -x test

# Copy the built JAR file
RUN cp build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"] 