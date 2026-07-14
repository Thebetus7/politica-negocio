# Paso 6: Despliegue del Backend y Base de Datos con Docker

En este paso final, configuraremos la contenerización completa del backend de **Spring Boot** y la base de datos **MongoDB** utilizando **Docker** y **Docker Compose**. Esto automatizará la compilación del código, la inicialización de la base de datos y la conexión con el almacenamiento en S3 que configuramos en los pasos previos.

---

## 6.1 — Crear el `Dockerfile` en el Backend

El `Dockerfile` define cómo se compila y empaqueta nuestra aplicación Spring Boot (Java 17) en una imagen Docker optimizada mediante un proceso de construcción multietapa (*Multi-stage build*).

Crea un archivo llamado **`Dockerfile`** (sin extensión) en la raíz del backend (`politica-negocio/`):

```dockerfile
# --- ETAPA 1: Construcción ---
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copiar el archivo pom.xml y descargar las dependencias para guardarlas en la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el archivo JAR omitiendo las pruebas unitarias
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: Ejecución ---
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiar el archivo JAR construido en la etapa anterior (asegurar que el nombre coincida con el pom.xml)
COPY --from=build /app/target/politica-negocio-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto configurado del backend (8081)
EXPOSE 8081

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 6.2 — Crear el `docker-compose.yml` en el Backend

Este archivo coordinará el contenedor del backend y el de la base de datos MongoDB, garantizando que se levanten en la misma red interna de Docker.

Crea un archivo llamado **`docker-compose.yml`** en la raíz del backend (`politica-negocio/`):

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:6.0-jammy
    container_name: politica_mongodb
    restart: always
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    environment:
      - MONGO_INITDB_DATABASE=politica_db

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: politica_backend
    restart: always
    ports:
      - "8081:8081"
    depends_on:
      - mongodb
    environment:
      # Conexión a MongoDB interna de Docker (apunta al servicio mongodb)
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/politica_db
      
      # URL de FastAPI (IA Gemini) - Reemplaza con tu túnel o IP de producción
      - FASTAPI_AI_BASE_URL=https://npwch9fd-8002.brs.devtunnels.ms
      
      # Integración de Almacenamiento en AWS S3 (Configurado en el Paso 2)
      - AWS_S3_ENDPOINT=https://s3.us-east-1.amazonaws.com
      - AWS_S3_REGION=us-east-1
      - AWS_S3_BUCKET=politica-docs-prod
      - AWS_S3_ACCESS_KEY=TU_AWS_ACCESS_KEY_ID
      - AWS_S3_SECRET_KEY=TU_AWS_SECRET_ACCESS_KEY

volumes:
  mongodb_data:
```

---

## 6.3 — Habilitar Variables de Entorno en Spring Boot

Para que Spring Boot cargue los parámetros del `docker-compose.yml` de forma correcta en producción, abre tu archivo [application.properties](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/resources/application.properties) y asegúrate de que esté estructurado para soportar fallbacks locales:

```properties
# MongoDB Config (Leerá la URI de Docker en producción o usará localhost en desarrollo local)
spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/politica_db}
spring.data.mongodb.auto-index-creation=true

# Server port
server.port=8081

# Microservicio FastAPI (IA Gemini)
fastapi.ai.base-url=${FASTAPI_AI_BASE_URL:http://localhost:8000}

# MinIO / AWS S3 Config
storage.s3.endpoint=${AWS_S3_ENDPOINT:http://localhost:9000}
storage.s3.region=${AWS_S3_REGION:us-east-1}
storage.s3.bucket=${AWS_S3_BUCKET:politica-docs}
storage.s3.access-key=${AWS_S3_ACCESS_KEY:minio}
storage.s3.secret-key=${AWS_S3_SECRET_KEY:minio12345}

spring.servlet.multipart.max-file-size=25MB
spring.servlet.multipart.max-request-size=25MB
```

---

## 6.4 — Comandos para Desplegar

Abre la terminal en la raíz del backend (`politica-negocio/`) y ejecuta:

### 1. Construir la imagen y levantar los contenedores en segundo plano:
```bash
docker compose up -d --build
```

### 2. Verificar que ambos contenedores estén corriendo (`Up`):
```bash
docker compose ps
```

### 3. Ver los logs en tiempo real para asegurar que Spring Boot inició con éxito y se conectó a MongoDB y S3:
```bash
docker compose logs -f backend
```

---

## 6.5 — Mantenimiento de Contenedores

### Detener los servicios sin borrar datos:
```bash
docker compose down
```

### Detener servicios y borrar base de datos para pruebas limpias:
```bash
docker compose down -v
```
*(El flag `-v` destruye el volumen de MongoDB. Úsalo con extrema precaución en producción).*

---

> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
