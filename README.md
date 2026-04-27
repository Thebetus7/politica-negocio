# 🍃 Backend - Política de Negocio (Spring Boot)

Servidor de API REST para la gestión de políticas de negocio, diagramas y autenticación.

## 🚀 Requisitos
- **Java 17**
- **MongoDB** (Puerto 27017)

## 🐳 Base de Datos con Docker (Recomendado)
Si tienes **Docker Desktop** instalado y abierto, puedes levantar MongoDB con este comando:

```bash
docker run -d --name mongodb-local -p 27017:27017 mongo:latest
```

## 🛠️ Ejecución
Para iniciar el servidor, abre una terminal en esta carpeta y ejecuta:

```bash
# En Windows (CMD o PowerShell)
./mvnw.cmd spring-boot:run

# En Linux o macOS
./mvnw spring-boot:run
```

El servidor estará disponible en: `http://localhost:8081`

## 📁 Endpoints Principales
- `POST /api/auth/login`: Autenticación de usuarios.
- `GET /api/auth/me`: Perfil del usuario autenticado.
- `GET /api/diagrams/base/{nombre}`: Obtener estructura de diagramas.
- `WS /ws-diagram`: WebSocket para sincronización en tiempo real.
