# Guía de Reinicio y Sembrado de la Base de Datos (MongoDB)

Esta guía explica el procedimiento para limpiar por completo la base de datos de MongoDB y volver a generar los datos iniciales necesarios (Sembrado / Seeding) en el proyecto **politica-negocio**.

---

## ⚙️ ¿Cómo funciona el proceso?

En el backend, contamos con un componente automático de Spring Boot llamado [DatabaseSeeder.java](file:///c:/EDBERTO/ULT%20SEMESTRE/SW1/1ER%20parcial/SW1_PN_1_2026/politica-negocio/src/main/java/com/example/politica_negocio/seeder/DatabaseSeeder.java). 

Este componente implementa la interfaz `CommandLineRunner`, lo que significa que **se ejecuta automáticamente cada vez que levantas el backend de Spring Boot**.
1. Al iniciar la aplicación, comprueba si las colecciones de departamentos y usuarios de MongoDB están vacías (`count() == 0`).
2. Si están vacías o no existen, inserta los registros semilla iniciales en la base de datos (roles, departamentos por defecto, usuario administrador y agentes de atención al cliente).

Por lo tanto, para reiniciar la base de datos y aplicar los datos semilla desde cero, solo necesitas **eliminar (Drop) la base de datos física**.

---

## 🗑️ Paso a Paso para Reiniciar la Base de Datos

Tienes dos alternativas principales para realizar esta acción:

### Alternativa A: Usando MongoDB Compass (Visual y Sencilla)

1. Abre **MongoDB Compass** y conéctate al servidor local (`mongodb://localhost:27017`).
2. En el panel izquierdo o en la pestaña central **Databases**, busca la base de datos llamada **`politica_db`**.
3. Pasa el cursor sobre el nombre de la base de datos `politica_db` y haz clic en el icono del **bote de basura (Delete database)**.
4. Se abrirá una ventana emergente de confirmación. Escribe exactamente el nombre de la base de datos (`politica_db`) para confirmar la eliminación y presiona **Drop Database**.
5. Detén tu servidor Spring Boot en la consola si estaba corriendo (`Ctrl + C`).
6. Vuelve a iniciar el backend ejecutando:
   ```bash
   mvnw.cmd spring-boot:run
   ```
   *Spring detectará que la base de datos ya no existe, la creará de nuevo en milisegundos y el seeder insertará los datos iniciales de forma automática.*

---

### Alternativa B: Desde la Consola (mongosh)

Si prefieres la terminal de comandos de MongoDB (`mongosh`):

1. Abre tu terminal de comandos y accede a la consola de Mongo ejecutando:
   ```bash
   mongosh
   ```
2. Cámbiate al contexto de tu base de datos:
   ```javascript
   use politica_db
   ```
3. Ejecuta el comando para eliminar la base de datos por completo:
   ```javascript
   db.dropDatabase()
   ```
   *(La consola debería responder con un mensaje confirmando la eliminación: `{ "ok" : 1, "dropped" : "politica_db" }`)*
4. Sal de la consola de Mongo escribiendo `exit`.
5. Reinicia tu backend de Spring Boot.

---

## 🔑 Credenciales Generadas por Defecto

Una vez reiniciada la base de datos, tendrás creados los siguientes datos iniciales:

* **Departamentos por defecto:**
  * Recursos Humanos
  * Operaciones
  * Atención al Cliente
* **Usuarios Semilla:**
  * **Administrador:** `admin@example.com` / contraseña: `admin123`
  * **Atención al Cliente 1:** `atencion1@example.com` / contraseña: `password`
  * **Atención al Cliente 2:** `atencion2@example.com` / contraseña: `password`
