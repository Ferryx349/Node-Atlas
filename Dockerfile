FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY mvnw .mvn pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -DskipTests

COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
