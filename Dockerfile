FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package dependency:copy-dependencies -DskipTests -DincludeScope=runtime -DoutputDirectory=/app/libs -pl vista -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/libs/ ./libs/
COPY --from=build /app/vista/target/*.jar ./libs/
COPY vista/ ./vista/
EXPOSE 8080
CMD ["java", "-cp", "libs/*", "mx.bambuybrillo.vista.Servidor"]
