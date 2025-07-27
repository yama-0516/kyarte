#!/bin/bash
# Build script for Render

# Build the application
./gradlew build -x test

# Create the JAR file
./gradlew bootJar 