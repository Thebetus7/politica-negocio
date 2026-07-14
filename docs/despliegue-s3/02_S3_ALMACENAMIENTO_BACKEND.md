# Paso 2: Configurar AWS S3 para Almacenamiento del Backend (Spring Boot)

En desarrollo local, el backend utiliza **MinIO** mediante un contenedor Docker para almacenar los documentos. En producción, migraremos esto de forma limpia a un bucket de **AWS S3** privado.

---

## 2.1 — Crear el Bucket Privado en S3

Sigue estos pasos en la consola de AWS:

1. Busca y selecciona **S3** en la consola de AWS.
2. Haz clic en **Create bucket** (Crear bucket).
3. Configura los parámetros generales:
   * **Bucket name:** `politica-docs-prod` (debe ser un nombre globalmente único en todo AWS).
   * **AWS Region:** Selecciona la misma región donde desplegarás tus servidores (ej. `us-east-1`).
4. **Object Ownership:** Selecciona **ACLs disabled (recommended)**.
5. **Block Public Access settings for this bucket:**
   * **Mantén activado** el check: **Block *all* public access**. 
   * *¿Por qué?* Los documentos cargados por los usuarios en el sistema contienen información sensible o de negocio y no deben ser accesibles públicamente por URL directa de S3. El backend controlará la descarga o generará enlaces firmados (Presigned URLs) si fuera necesario.
6. **Bucket Versioning:** Selecciona **Enable** (Habilitar). Esto te permitirá recuperar versiones anteriores de un archivo si se modifica o elimina accidentalmente.
7. **Encryption:** Deja activado **Server-side encryption with Amazon S3-managed keys (SSE-S3)**.
8. Haz clic en **Create bucket**.

---

## 2.2 — Configurar CORS en el Bucket de S3

Si tu frontend (Angular) o aplicación móvil (Flutter) necesita acceder a recursos del bucket de manera directa (por ejemplo, descargar archivos binarios mediante peticiones HTTP desde el navegador o app), debes configurar **CORS (Cross-Origin Resource Sharing)** en el bucket:

1. Entra al bucket `politica-docs-prod` en la consola de S3.
2. Haz clic en la pestaña **Permissions** (Permisos).
3. Baja hasta la sección **Cross-origin resource sharing (CORS)** y haz clic en **Edit**.
4. Pega la siguiente configuración en formato JSON:

```json
[
    {
        "AllowedHeaders": [
            "*"
        ],
        "AllowedMethods": [
            "GET",
            "PUT",
            "POST",
            "DELETE",
            "HEAD"
        ],
        "AllowedOrigins": [
            "*"
        ],
        "ExposeHeaders": [
            "ETag",
            "Content-Type",
            "Content-Length"
        ],
        "MaxAgeSeconds": 3000
    }
]
```
> *Nota: En un entorno de producción estricto, puedes reemplazar `"*"` en `AllowedOrigins` por el dominio HTTPS oficial de tu frontend (ej. `https://tuapp.com`).*

5. Haz clic en **Save changes** (Guardar cambios).

---

## 2.3 — Integración en Spring Boot (Sin modificar código Java)

Gracias a que el backend utiliza el SDK de AWS S3 de manera estándar, **no necesitas cambiar una sola línea de código en Java**. La clase [S3Config.java](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/java/com/example/politica_negocio/config/S3Config.java) leerá los valores directamente de la configuración.

### Opción A: Modificando `application.properties` (No recomendado para commits públicos)

Edita el archivo [application.properties](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/resources/application.properties) antes de empaquetar la aplicación para producción:

```properties
# Reemplazar por el endpoint regional oficial de AWS S3
storage.s3.endpoint=https://s3.us-east-1.amazonaws.com

# Tu región de AWS S3
storage.s3.region=us-east-1

# El nombre del bucket único que creaste en el paso 2.1
storage.s3.bucket=politica-docs-prod

# Las credenciales del usuario IAM generadas en el Paso 1
storage.s3.access-key=TU_AWS_ACCESS_KEY_ID
storage.s3.secret-key=TU_AWS_SECRET_ACCESS_KEY
```

### Opción B: Inyectando Variables de Entorno (Recomendado y Seguro)

La mejor práctica es mantener el archivo [application.properties](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/resources/application.properties) configurado para leer variables del sistema operativo. Esto evita exponer credenciales en repositorios de Git.

1. **Modifica tu `application.properties` para soportar variables de entorno:**
```properties
storage.s3.endpoint=${AWS_S3_ENDPOINT:http://localhost:9000}
storage.s3.region=${AWS_S3_REGION:us-east-2}
storage.s3.bucket=${AWS_S3_BUCKET:politica-docs}
storage.s3.access-key=${AWS_S3_ACCESS_KEY:minio}
storage.s3.secret-key=${AWS_S3_SECRET_KEY:minio12345}
```

2. **Define las variables en el servidor de producción (EC2 u otro servicio de hospedaje):**

Si corres tu backend en una instancia EC2 de Linux, puedes exportarlas en el perfil del sistema o inyectarlas en el comando de ejecución:

```bash
export AWS_S3_ENDPOINT=https://s3.us-east-1.amazonaws.com
export AWS_S3_REGION=us-east-2
export AWS_S3_BUCKET=politica-docs-prod
export AWS_S3_ACCESS_KEY=TU_AWS_ACCESS_KEY_ID
export AWS_S3_SECRET_KEY=TU_AWS_SECRET_ACCESS_KEY
```

Si ejecutas el backend con Docker o Docker Compose en el servidor, agrégalas en la sección `environment` de tu archivo `docker-compose.yml`:

```yaml
services:
  web:
    image: politica-negocio-backend:latest
    ports:
      - "8081:8081"
    environment:
      - AWS_S3_ENDPOINT=https://s3.us-east-1.amazonaws.com
      - AWS_S3_REGION=us-east-1
      - AWS_S3_BUCKET=politica-docs-prod
      - AWS_S3_ACCESS_KEY=TU_AWS_ACCESS_KEY_ID
      - AWS_S3_SECRET_KEY=TU_AWS_SECRET_ACCESS_KEY
```

---

> **Siguiente paso:** [03_CREAR_INSTANCIA_EC2.md](./03_CREAR_INSTANCIA_EC2.md)  


> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
