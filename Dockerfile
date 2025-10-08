# Use Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run the packaged app
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/domain_checker.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
