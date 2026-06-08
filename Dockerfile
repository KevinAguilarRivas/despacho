# Etapa 1: Build con Maven
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final liviana
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el jar generado
COPY --from=build /app/target/*.jar app.jar

# Crear carpeta EFS (será el punto de montaje del volumen EFS)
RUN mkdir -p /app/efs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]