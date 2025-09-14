FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=stats-service/target/stats-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} stats-service.jar
ENTRYPOINT ["java", "-jar", "/stats-service.jar"]