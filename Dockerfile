# --- ETAPA 1: Construcción ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar el archivo pom.xml y descargar las dependencias para guardarlas en la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el archivo JAR omitiendo las pruebas unitarias
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: Ejecución ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar el archivo JAR construido en la etapa anterior (asegurar que el nombre coincida con el pom.xml)
COPY --from=build /app/target/politica-negocio-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto configurado del backend (8081)
EXPOSE 8081

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]