FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY ISMS/pom.xml .
COPY ISMS/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ISMS-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
