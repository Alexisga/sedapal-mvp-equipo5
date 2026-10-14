FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --gid 10001 sedapal && useradd --uid 10001 --gid sedapal --no-create-home sedapal
WORKDIR /app
COPY --from=build --chown=sedapal:sedapal /build/target/sedapal-reclamos-0.1.2-SNAPSHOT.jar /app/app.jar
USER 10001:10001
ENV SPRING_PROFILES_ACTIVE=cloud-demo
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
