# Build the Java application with Maven.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependency metadata first so Docker can cache dependency downloads.
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# Run only the lightweight Java web server.
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/classes ./target/classes

# Render provides PORT at runtime. The app falls back to 8080 locally.
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "target/classes", "web.SimulationWebServer"]
