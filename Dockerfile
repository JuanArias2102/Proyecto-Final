FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/demo-1.0-SNAPSHOT.jar /app/app.jar

ENV PORT=4567 \
    TASA_DEFECTO=4000 \
    DATA_FILE=/app/data/datos.json

VOLUME ["/app/data"]
EXPOSE 4567

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
