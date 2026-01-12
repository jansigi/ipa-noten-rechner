FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

COPY src ./src
RUN ./gradlew installDist

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/build/install/backend ./

EXPOSE 8080
CMD ["./bin/backend"]
