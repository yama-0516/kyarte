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

# Copy the built JAR file (Spring Boot executable JAR)
RUN cp build/libs/kyarte-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"] 