# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar la caché de capas de Docker:
# si no cambian las dependencias, no se vuelven a descargar en cada build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: imagen final, solo el JRE + el .jar ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
