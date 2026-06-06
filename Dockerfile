FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY src/main/java src/main/java
COPY src/main/resources src/main/resources

RUN mkdir -p /app/classes \
    && find src/main/java -name '*.java' ! -path '*/visualization/*' -print > /tmp/sources.txt \
    && javac -d /app/classes @/tmp/sources.txt \
    && if [ -d src/main/resources ]; then cp -R src/main/resources/* /app/classes/; fi

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/classes /app/classes

EXPOSE 8080

CMD ["java", "-cp", "/app/classes", "web.SimulationWebServer"]
