# Dockerfile
FROM eclipse-temurin:21-jdk

ARG JAR_FILE=target/dialog-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
