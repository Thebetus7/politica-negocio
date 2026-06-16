# MinIO — almacenamiento S3 local

MinIO es compatible con la API S3. Spring Boot sube los documentos de formularios (campo `archivo`) aquí.

## 1. Levantar MinIO

Desde la carpeta `politica-negocio/`:

```bash
docker compose -f docker-compose.minio.yml up -d
```

- **API S3:** http://localhost:9000
- **Consola web:** http://localhost:9001
- **Usuario:** `minio`
- **Contraseña:** `minio12345`

## 2. Crear el bucket

1. Abre http://localhost:9001 e inicia sesión.
2. **Buckets** → **Create Bucket**.
3. Nombre: `politica-docs`
4. Deja el bucket **privado** (acceso vía URLs firmadas desde Spring).

## 3. Configuración Spring Boot

En `src/main/resources/application.properties`:

```properties
storage.s3.endpoint=http://localhost:9000
storage.s3.region=us-east-1
storage.s3.bucket=politica-docs
storage.s3.access-key=minio
storage.s3.secret-key=minio12345
```

## 4. Probar upload

Con el backend en marcha y JWT de un usuario autenticado:

```bash
curl -X POST http://localhost:8081/api/documentos \
  -H "Authorization: Bearer TU_JWT" \
  -F "file=@./ejemplo.pdf" \
  -F "actividadId=..." \
  -F "portafolioId=..."
```

Respuesta: `{ "id", "nombreOriginal", "mimeType", "size", "downloadUrl" }`.

## 5. Detener

```bash
docker compose -f docker-compose.minio.yml down
```

Los datos persisten en el volumen `minio_data`.

## Producción (AWS S3)

Cambia `storage.s3.endpoint` por el endpoint de AWS (o déjalo vacío para el default) y usa credenciales IAM. El mismo código `DocumentoService` funciona sin cambios.
