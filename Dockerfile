# --------------------------
# Build Stage
# --------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --------------------------
# Runtime Stage
# --------------------------
FROM eclipse-temurin:17-jdk AS runtime
WORKDIR /app
COPY --from=build /build/target/p2p-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
