# Paso 5: Subir Código y Desplegar el Backend con Docker

Una vez que configuraste tu instancia EC2 e instalaste las herramientas (Git, Docker, Compose), el flujo consiste en descargar el código de tu repositorio en el servidor de AWS y levantar los servicios.

---

## 5.1 — Descargar el Código en el Servidor (EC2)

1. Conéctate a tu servidor EC2 por SSH desde tu terminal local (como se explicó en el Paso 4):
   ```bash
   ssh -i politica-backend-key.pem ec2-user@TU_IP_PUBLICA_EC2
   ```
2. Clona tu repositorio de Git en la carpeta de usuario de la EC2 (reemplaza por la URL de tu repositorio real):
   ```bash
   git clone https://github.com/tu-usuario/tu-repositorio.git politica-negocio-backend
   ```
3. Entra a la carpeta del backend donde se encuentran el `Dockerfile` y el `docker-compose.yml`:
   ```bash
   cd politica-negocio-backend/politica-negocio
   ```

---

## 5.2 — Confirmar la Configuración de Docker

Asegúrate de que los archivos `Dockerfile` y `docker-compose.yml` estén creados en este directorio:

### 📄 `Dockerfile`
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

### 📄 `docker-compose.yml`
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
      - AWS_S3_REGION=us-east-2
      - AWS_S3_BUCKET=politica-docs-prod  # El nombre único de tu bucket creado en el Paso 2
      
      # Credenciales de acceso programático de AWS (Obtenidas en el Paso 1)
      # Las encuentras en el archivo credentials.csv descargado al crear el usuario IAM
      - AWS_S3_ACCESS_KEY=TU_AWS_ACCESS_KEY_ID
      - AWS_S3_SECRET_KEY=TU_AWS_SECRET_ACCESS_KEY

volumes:
  mongodb_data:
```

---

## 5.3 — Habilitar Variables de Entorno en Spring Boot

Comprueba que tu archivo [application.properties](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/resources/application.properties) esté configurado para soportar las variables que inyectamos en el docker-compose:

```properties
spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/politica_db}
spring.data.mongodb.auto-index-creation=true
server.port=8081
fastapi.ai.base-url=${FASTAPI_AI_BASE_URL:http://localhost:8000}
storage.s3.endpoint=${AWS_S3_ENDPOINT:http://localhost:9000}
storage.s3.region=${AWS_S3_REGION:us-east-2}
storage.s3.bucket=${AWS_S3_BUCKET:politica-docs}
storage.s3.access-key=${AWS_S3_ACCESS_KEY:minio}
storage.s3.secret-key=${AWS_S3_SECRET_KEY:minio12345}
spring.servlet.multipart.max-file-size=25MB
spring.servlet.multipart.max-request-size=25MB
```

---

## 5.4 — Levantar y Verificar Contenedores

Una vez dentro de la carpeta del backend, inicia los servicios:

### 1. Construir las imágenes y levantar contenedores en segundo plano:
```bash
docker-compose up -d --build
```

### 2. Confirmar que los contenedores están corriendo (`Up`):
```bash
docker-compose ps
```

### 3. Monitorear el inicio del servidor y buscar posibles errores de Spring Boot:
```bash
docker-compose logs -f backend
```
*Deberías ver la consola de Spring Boot cargando las clases e indicando al final: `Started PoliticaNegocioApplication in X seconds`.*

### 4. Probar la API de producción:
Abre tu navegador local e ingresa a (reemplaza por la IP de tu EC2):
```text
http://TU_IP_PUBLICA_EC2:8081/api/auth/... (o la ruta que desees probar)
```

---

## 5.5 — Comandos de Mantenimiento

### Ver logs de la base de datos MongoDB:
```bash
docker-compose logs -f mongodb
```

### Detener los servicios temporalmente:
```bash
docker-compose down
```

---

> **Siguiente paso:** [06_MANTENIMIENTO_Y_APAGADO.md](./06_MANTENIMIENTO_Y_APAGADO.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
