# Use Gradle image to build the application
FROM gradle:jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# Use OpenJDK image to run the application
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/forex-data-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]