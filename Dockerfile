# --------------------------
# Build Stage
# --------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

# Copy Maven config and source code
COPY pom.xml .
COPY src ./src

# Build the application (skip tests)
RUN mvn clean package -DskipTests

# --------------------------
# Runtime Stage
# --------------------------
FROM eclipse-temurin:17-jdk-slim
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/target/p2p-1.0-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
