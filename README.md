# Backend - Gestión de Políticas de Negocio (Spring Boot)

Este es el módulo backend del sistema de **Gestión de Políticas de Negocio**, desarrollado en **Java 17** utilizando **Spring Boot 3.4.1** y **MongoDB** como base de datos persistente.

---

## 🛠️ Requisitos Previos

Antes de arrancar el backend, asegúrate de tener instalado y configurado:

1. **Java Development Kit (JDK) 17**:
   * Asegúrate de tener configurada la variable de entorno `JAVA_HOME`.
   * Verifica la versión ejecutando: `java -version`
2. **MongoDB**:
   * El servicio de MongoDB debe estar corriendo localmente en el puerto por defecto `27017` (URI: `mongodb://localhost:27017/politica_db`).
3. **Docker (Opcional - Para almacenamiento de archivos)**:
   * Necesario si utilizas la carga de documentos de formularios (usa un contenedor MinIO).

---

## 🚀 Pasos para Levantar el Backend

### 1. Iniciar la base de datos (MongoDB)
Asegúrate de que tu base de datos MongoDB está activa. Si usas Docker para levantarla rápidamente:
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 2. Iniciar MinIO (Opcional - Para documentos)
Si necesitas habilitar el almacenamiento S3 de documentos de formularios, levanta el contenedor de MinIO ejecutando:
```bash
docker compose -f docker-compose.minio.yml up -d
```

### 3. Ejecutar el Servidor Spring Boot
Usa el Maven Wrapper incluido en el proyecto para levantar el servidor de desarrollo:

* **En Windows (CMD / PowerShell)**:
  ```bash
  mvnw.cmd spring-boot:run
  ```
* **En Linux / macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```

El backend se iniciará en el puerto **`8081`** (por defecto configurado en [application.properties](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/resources/application.properties#L8)).

---

## 🔑 Credenciales Semilla y Primer Inicio

La primera vez que levantes el servidor con la base de datos vacía, se ejecutará el sembrado automático ([DatabaseSeeder.java](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/java/com/example/politica_negocio/seeder/DatabaseSeeder.java)). 

Puedes iniciar sesión con las siguientes credenciales:
* **Administrador:** `admin@example.com` / contraseña: `admin123`
* **Agentes de Atención:** `atencion1@example.com`, `atencion2@example.com` / contraseña: `password`

---

## 📦 Comandos Útiles de Maven

* **Limpiar compilaciones previas y empaquetar el proyecto en un archivo `.jar`**:
  ```bash
  mvnw.cmd clean package
  ```
* **Ejecutar las pruebas unitarias**:
  ```bash
  mvnw.cmd test
  ```

---

## 📖 Documentación Relacionada
Para comprender a fondo la lógica y los procesos del sistema, consulta los siguientes manuales en la carpeta de documentación:
* **[Flujo de Arquitectura y Datos](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/docs/flujo_arquitectura.md)**: Explicación de cómo interactúan los Controllers, Services y Repositories con MongoDB.
* **[Guía de Reinicio de la Base de Datos](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/docs/reinicio_base_datos.md)**: Cómo borrar los datos y volver a aplicar los usuarios iniciales semilla.
