# Paso 3: Desplegar el Frontend (Angular) en AWS S3

Un bucket de **Amazon S3** es el lugar ideal para hospedar aplicaciones frontales basadas en Single Page Applications (SPA) como **Angular**, ya que es extremadamente económico, altamente disponible y escala automáticamente a millones de usuarios sin necesidad de administrar servidores web (como Nginx o Apache).

---

## 3.1 — Crear el Bucket para Hosting en S3

Sigue estos pasos en la consola de AWS:

1. Ve al servicio de **S3** y haz clic en **Create bucket** (Crear bucket).
2. Asigna los parámetros generales:
   * **Bucket name:** `politica-frontend-prod` (recuerda que debe ser único).
   * **AWS Region:** Selecciona tu región preferida (ej. `us-east-1`).
3. **Block Public Access settings for this bucket:**
   * **DESMARCA** la opción: **Block *all* public access**.
   * Marca la casilla de advertencia inferior: *"I acknowledge that the current settings might result in this bucket and the objects within it becoming public"*.
   * *¿Por qué?* Para que los navegadores puedan descargar tu HTML, CSS y JS, el bucket necesita permitir acceso de lectura público.
4. Deja el resto de las opciones por defecto y haz clic en **Create bucket**.

---

## 3.2 — Habilitar Static Website Hosting

Una vez creado el bucket:

1. Haz clic en el bucket `politica-frontend-prod`.
2. Dirígete a la pestaña **Properties** (Propiedades).
3. Desplázate hasta la última sección llamada **Static website hosting** y haz clic en **Edit**.
4. Configura el hosting:
   * **Static website hosting:** Selecciona **Enable** (Habilitar).
   * **Hosting type:** Selecciona **Host a static website**.
   * **Index document:** Escribe `index.html`.
   * **Error document:** Escribe `index.html`.
     * > **¡CRÍTICO PARA ANGULAR!** En una SPA, rutas físicas como `/dashboard` o `/politicas` no existen en S3. Si un usuario refresca la página en una de estas rutas, S3 devolverá un error 404. Al poner `index.html` en el documento de error, le indicamos a S3 que cualquier ruta inexistente la envíe a `index.html`, donde el enrutador de Angular se encargará de mostrar la página correcta.
5. Haz clic en **Save changes** (Guardar cambios).
6. Al guardar, vuelve a la sección **Static website hosting** abajo en la pestaña Properties. Verás un enlace bajo el título **Bucket website endpoint** (ej. `http://politica-frontend-prod.s3-website-us-east-1.amazonaws.com`). **Copia este enlace**, será la URL pública temporal de tu frontend.

---

## 3.3 — Configurar Política de Acceso Público al Bucket (Bucket Policy)

Aunque desmarcaste el bloqueo de acceso público, debes definir explícitamente una política que permita a cualquiera leer los archivos del bucket:

1. Ve a la pestaña **Permissions** (Permisos) del bucket.
2. En la sección **Bucket policy**, haz clic en **Edit**.
3. Pega el siguiente JSON (reemplaza `politica-frontend-prod` por el nombre exacto de tu bucket):

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::politica-frontend-prod/*"
        }
    ]
}
```

4. Haz clic en **Save changes** (Guardar cambios). Tu bucket ahora mostrará una etiqueta roja indicando **Public** (Público).

---

## 3.4 — Compilar la Aplicación de Angular

Antes de subir los archivos, compila la aplicación para producción en tu PC local:

1. Abre una terminal en tu computadora y navega al directorio del frontend:
   ```bash
   cd politica-negocio-frontend
   ```
2. Asegúrate de configurar la URL del backend en tu archivo de variables de entorno de producción (ej. `src/environments/environment.prod.ts`) apuntando al endpoint de tu backend en producción (ej. `https://api.tuapp.com` o la IP de tu EC2).
3. Compila el proyecto con optimizaciones de producción:
   ```bash
   npm run build -- --configuration production
   ```
   *(O si usas el CLI de Angular directamente: `ng build --configuration production`)*
4. Esto creará los archivos de distribución en una subcarpeta dentro del directorio `dist/` (por ejemplo: `dist/politica-negocio-frontend/browser/`).

---

## 3.5 — Subir los Archivos a S3

### Opción A: A través de la Consola de AWS (Manual)
1. Entra al bucket `politica-frontend-prod` en la consola de S3.
2. Ve a la pestaña **Objects** (Objetos).
3. Abre la carpeta compilada en tu PC (`dist/politica-negocio-frontend/browser/`).
4. Selecciona **todos** los archivos y carpetas internos (deben verse archivos como `index.html`, `main.js`, `styles.css`, `assets/`, etc.).
5. Arrástralos y suéltalos directamente en la consola web de S3.
6. Haz clic en **Upload** (Cargar) y espera a que termine.

### Opción B: Usando AWS CLI (Automatizado y Recomendado)
Si tienes instalado **AWS CLI** en tu PC, puedes subir los archivos con un solo comando usando las credenciales IAM del Paso 1:

1. Configura tus credenciales (si no lo has hecho):
   ```bash
   aws configure
   ```
   Introduce tu `AWS Access Key ID`, `AWS Secret Access Key`, tu región por defecto (ej. `us-east-1`) y formato de salida (deja vacío).
2. Sube y sincroniza los archivos de la carpeta compilada al bucket:
   ```bash
   aws s3 sync dist/politica-negocio-frontend/browser/ s3://politica-frontend-prod --delete
   ```
   * *¿Qué hace `--delete`?* Sube archivos nuevos, actualiza los modificados y borra del bucket los archivos antiguos que ya no existan en tu compilación local, manteniendo el bucket limpio.

¡Felicidades! Abre el enlace del **Bucket website endpoint** copiado en el paso 3.2 en tu navegador para ver tu frontend de Angular funcionando en producción.

---

> **Siguiente paso:** [04_AWS_CLOUDFRONT.md](./04_AWS_CLOUDFRONT.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
