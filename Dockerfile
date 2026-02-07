FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY library-data/build.gradle.kts library-data/
COPY library-service-application/build.gradle.kts library-service-application/

RUN ./gradlew :library-service-application:dependencies --no-daemon

COPY . .
RUN ./gradlew :library-service-application:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/library-service-application/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]