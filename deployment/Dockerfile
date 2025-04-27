FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace/app

# Copy gradle files for dependency resolution
COPY ../gradlew .
COPY ../gradle gradle
COPY ../build.gradle .
COPY ../settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies

# Copy source code
COPY ../src src

# Build the application
RUN ./gradlew build -x test

# For the runtime image
FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]