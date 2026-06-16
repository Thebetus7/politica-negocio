# Sistema de Gestión de Políticas de Negocio — Backend

Servidor API REST (Spring Boot 3.x, Java 17, MongoDB) para gestión de políticas, diagramas, trámites, documentos y autenticación.

Este módulo es el **backend principal** del ecosistema. Los demás componentes viven en carpetas hermanas del monorepo.

| Módulo | Carpeta | Puerto |
|--------|---------|--------|
| **Backend** (este) | `politica-negocio/` | `8081` |
| Frontend web | `politica-negocio-frontend/` | `4200` |
| App móvil | `politica_negocio_movil/` | — |
| IA (Gemini/voz) | `fastapi-ai/` | `8000` |

---

## Requisitos

- **Java 17**
- **MongoDB** (puerto 27017)
- **Docker Desktop** (opcional: MongoDB y MinIO)

---

## MongoDB local

Guía completa de instalación en Windows, conexión, reset e inspección de datos:

**[README_MONGODB_LOCAL.md](./README_MONGODB_LOCAL.md)**

Alternativa con Docker:

```bash
docker run -d --name mongodb-local -p 27017:27017 mongo:latest
```

---

## MinIO (documentos de formularios)

Los campos tipo `archivo` se almacenan en MinIO (compatible S3). Guía:

**[docs/MINIO_SETUP.md](./docs/MINIO_SETUP.md)**

Desde esta carpeta (`politica-negocio/`):

```bash
docker compose -f docker-compose.minio.yml up -d
```

---

## Ejecución

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

URL: `http://localhost:8081` — API base: `http://localhost:8081/api`

---

## Documentación técnica

| Archivo | Contenido |
|---------|-----------|
| [FLUJO_TRAMITE.md](./FLUJO_TRAMITE.md) | Flujo end-to-end AC → FU → progreso móvil |
| [docs/MINIO_SETUP.md](./docs/MINIO_SETUP.md) | Almacenamiento S3/MinIO para documentos |
| [docs/DISEÑO_MONGODB.md](./docs/DISE%C3%91O_MONGODB.md) | Modelo de datos MongoDB |
| [docs/DIAGRAMA_WORKFLOW_LOGICA_TECNICA.md](./docs/DIAGRAMA_WORKFLOW_LOGICA_TECNICA.md) | Lógica del editor de diagramas |
| [esquemaxml.xsd](./esquemaxml.xsd) | Esquema XML de políticas |

---

## Endpoints principales

- `POST /api/auth/login` — Autenticación
- `GET /api/auth/me` — Perfil del usuario autenticado
- `GET /api/politicas/public` — Políticas públicas (móvil)
- `POST /api/portafolios` — Crear trámite
- `GET /api/portafolios/{id}/progreso` — Progreso del trámite
- `POST /api/documentos` — Subir documento (multipart)
- `POST /api/voz/llenar-formulario/{formularioId}` — Prellenar formulario por voz
- `WS /ws-diagram` — Colaboración en editor (SockJS)
- `WS /ws-native` — STOMP nativo (móvil)

---

## Levantar el ecosistema completo

```bash
# 1. Backend (esta carpeta)
./mvnw.cmd spring-boot:run

# 2. Frontend
cd ../politica-negocio-frontend && npm install && npm start

# 3. Móvil
cd ../politica_negocio_movil && flutter pub get && flutter run

# 4. IA
cd ../fastapi-ai && pip install -r requirements.txt && uvicorn app.main:app --reload --port 8000
```
