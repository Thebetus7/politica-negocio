# Ejecución de MinIO para Almacenamiento Local (S3)

Este documento detalla los pasos para levantar e inicializar **MinIO** en tu entorno local mediante Docker, simulando el almacenamiento de objetos S3 utilizado en producción.

---

## 1. Prerrequisitos

*   **Docker Desktop** instalado y en ejecución en tu sistema.
*   **Docker Compose** disponible en la terminal.

---

## 2. Levantar el Servicio local de MinIO

Para iniciar MinIO, ejecuta el siguiente comando en la raíz del backend (`politica-negocio`), donde se ubica el archivo `docker-compose.minio.yml`:

```bash
docker compose -f docker-compose.minio.yml up -d
```

### Puertos Expuestos:
*   **`9000`**: Puerto de la API S3 (utilizado por el backend de Spring Boot para subir/bajar archivos).
*   **`9001`**: Puerto de la Consola de Administración Web de MinIO.

---

## 3. Configuración Inicial del Bucket

Una vez que el contenedor esté corriendo:

1.  Abre tu navegador e ingresa a la consola web: **[http://localhost:9001](http://localhost:9001)**.
2.  Inicia sesión con las credenciales por defecto:
    *   **Usuario (Access Key):** `minio`
    *   **Contraseña (Secret Key):** `minio12345`
3.  Ve al panel de **Buckets** y haz clic en **Create Bucket**.
4.  Nombra al bucket exactamente como **`politica-docs`** (es el bucket esperado por el backend en su configuración).
5.  *(Recomendado)* Configura las políticas de acceso del bucket como **public** o de lectura-escritura si requieres acceso directo a URLs de vista previa.

---

## 4. Configuración en el Backend (Spring Boot)

La integración está preconfigurada en el archivo `application.properties` con los siguientes parámetros correspondientes a MinIO local:

```properties
storage.s3.endpoint=http://localhost:9000
storage.s3.region=us-east-1
storage.s3.bucket=politica-docs
storage.s3.access-key=minio
storage.s3.secret-key=minio12345
spring.servlet.multipart.max-file-size=25MB
spring.servlet.multipart.max-request-size=25MB
```

---

## 5. Detener el Servicio

Cuando termines tu sesión de desarrollo, puedes apagar el contenedor ejecutando:

```bash
docker compose -f docker-compose.minio.yml down
```

---

## 6. Migración a AWS S3 (Producción)

Para migrar del MinIO local a un bucket de **AWS S3 real en producción**, **no necesitas modificar el código Java**. Toda la migración se realiza de forma limpia ajustando los parámetros en `application.properties` (o definiendo variables de entorno equivalentes):

### 1. Variables a Modificar en `application.properties`:

Cambia los valores locales por los de tu cuenta de AWS:

```properties
# Reemplazar por el endpoint regional oficial de AWS S3
storage.s3.endpoint=https://s3.us-east-1.amazonaws.com

# Tu región de AWS S3
storage.s3.region=us-east-1

# El nombre de tu bucket real en AWS
storage.s3.bucket=nombre-de-tu-bucket-real

# Tus credenciales IAM de AWS con permisos de lectura/escritura en S3
storage.s3.access-key=TU_AWS_ACCESS_KEY_ID
storage.s3.secret-key=TU_AWS_SECRET_ACCESS_KEY
```

> [!NOTE]
> En AWS S3 real se utiliza por defecto el direccionamiento de tipo *Virtual Hosted-Style* (`https://bucket.s3.amazonaws.com`), pero la configuración `pathStyleAccessEnabled(true)` en `S3Config.java` sigue siendo compatible y permitida por AWS. Si deseas forzar el estándar de AWS en producción, puedes inyectar un parámetro condicional en `S3Config.java` para activar/desactivar `pathStyleAccessEnabled` según el entorno.

