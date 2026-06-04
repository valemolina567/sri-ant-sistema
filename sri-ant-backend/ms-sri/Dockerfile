# Dockerfile – aplicable a los tres microservicios Spring Boot
# Construir con: docker build -t ms-sri . (desde el directorio del microservicio)

# ── Etapa 1: compilar con Maven ───────────────────────────────────────────────
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Etapa 2: imagen de ejecución (JRE mínimo) ────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario no-root para producción
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
