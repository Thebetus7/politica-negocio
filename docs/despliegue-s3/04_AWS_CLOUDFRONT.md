# Paso 4: Configurar AWS CloudFront (CDN & HTTPS) para el Frontend

Aunque tu sitio web estático ya es accesible en HTTP usando el endpoint de S3, en producción es obligatorio utilizar **HTTPS (SSL/TLS)** por motivos de seguridad y SEO. Además, necesitas una red de distribución de contenido (**CDN**) para acelerar la carga de la aplicación en cualquier parte del mundo.

Para ello utilizaremos **AWS CloudFront** para servir los archivos de nuestro bucket S3.

---

## 4.1 — ¿Por qué CloudFront es Obligatorio en Producción?

1. **HTTPS Seguro:** S3 no soporta certificados SSL personalizados de forma nativa para sitios web estáticos. CloudFront permite asociar certificados SSL gratuitos a través de AWS Certificate Manager (ACM).
2. **CDN Global:** CloudFront almacena en caché tu frontend en ubicaciones de borde (Edge Locations) de AWS en todo el mundo, reduciendo el tiempo de carga a milisegundos.
3. **Seguridad Avanzada (OAC):** Permite cerrar de nuevo el acceso público de S3, obligando a que solo CloudFront pueda leer el bucket mediante un Control de Acceso de Origen (OAC).

---

## 4.2 — Paso a Paso: Crear la Distribución de CloudFront

1. Busca y selecciona **CloudFront** en la consola de AWS.
2. Haz clic en **Create distribution** (Crear distribución).
3. **Origin (Origen):**
   * **Origin domain:** Haz clic en el cuadro de búsqueda y selecciona tu bucket S3: `politica-frontend-prod.s3.us-east-1.amazonaws.com`.
   * **Origin access:** Selecciona **Origin access control settings (recommended)** (Control de acceso de origen).
     * Haz clic en **Create new OAC** (Crear control de acceso de origen), deja el nombre por defecto y haz clic en **Create**.
     * *(Esto asegurará que nadie pueda saltarse HTTPS/CloudFront y acceder directamente a S3).*
4. **Default cache behavior (Comportamiento de caché por defecto):**
   * **Viewer protocol policy:** Selecciona **Redirect HTTP to HTTPS** (Redirigir HTTP a HTTPS).
   * **Allowed HTTP methods:** Selecciona `GET, HEAD`.
5. **Web Application Firewall (WAF):**
   * Selecciona **Do not enable security protections** por ahora para evitar costos de WAF (puedes activarlo más adelante si requieres protección contra ataques DDoS).
6. **Settings (Configuración):**
   * **Alternative domain name (CNAME):** Si tienes un dominio propio (ej. `app.politicanegocio.com`), agrégalo aquí.
   * **Custom SSL certificate:** Si agregaste un dominio personalizado, solicita un certificado gratuito en **AWS Certificate Manager (ACM)** (¡Debe ser solicitado en la región `us-east-1` obligatoriamente!) y selecciónalo en este campo.
   * **Default root object:** Escribe `index.html`.
7. Haz clic en **Create distribution** (Crear distribución).

---

## 4.3 — Actualizar la Política del Bucket de S3

Al crear la distribución con OAC (Origin Access Control), AWS te proporcionará una política de S3 lista para copiar. Esta política permite a CloudFront leer del bucket S3 aunque sea privado.

1. Al finalizar la creación, verás un banner amarillo que dice *"The S3 bucket policy needs to be updated..."*. Haz clic en el botón **Copy policy** (Copiar política).
2. Ve a tu bucket de S3 `politica-frontend-prod` -> pestaña **Permissions** (Permisos) -> **Bucket policy** (Política del bucket) -> **Edit**.
3. Pega el JSON copiado (sobreescribiendo la política de lectura pública que creamos en el paso 3.3).
4. Guarda los cambios.
5. *(Opcional)* En la pestaña de permisos del bucket S3, ahora puedes volver a **activar** "Block all public access" (Bloquear todo el acceso público). De este modo, tu bucket vuelve a ser completamente privado y solo CloudFront puede leer su contenido de forma segura.

---

## 4.4 — Configurar el Enrutamiento de la SPA (Páginas de Error)

Debido a que Angular es una SPA, si un usuario ingresa directamente a una ruta interna como `https://d12345.cloudfront.net/dashboard`, CloudFront irá al bucket S3 a buscar el archivo físico `/dashboard` (que no existe) y S3 responderá con un error **403 Access Denied** (ya que el bucket es privado por OAC).

Debemos instruir a CloudFront para que devuelva `index.html` con un estado HTTP 200 en lugar de un error.

1. Dentro de tu distribución de CloudFront, dirígete a la pestaña **Error pages** (Páginas de error).
2. Haz clic en **Create custom error response** (Crear respuesta de error personalizada).
3. Configura la regla para el error **403: Forbidden**:
   * **HTTP error code:** `403: Forbidden`.
   * **Customize error response:** Selecciona **Yes** (Sí).
   * **Response page path:** Escribe `/index.html`.
   * **HTTP Response code:** Selecciona **200: OK**.
4. Haz clic en **Create**.
5. Repite el proceso haciendo clic en **Create custom error response** para el error **404: Not Found**:
   * **HTTP error code:** `404: Not Found`.
   * **Customize error response:** Selecciona **Yes** (Sí).
   * **Response page path:** Escribe `/index.html`.
   * **HTTP Response code:** Selecciona **200: OK**.
6. Haz clic en **Create**.

---

## 4.5 — Verificar el Despliegue

1. Vuelve a la pestaña **Details** (Detalles) de tu distribución.
2. Copia el valor de **Distribution domain name** (ej. `https://d3a1b2c3d4e5f6.cloudfront.net`).
3. Pégalo en tu navegador.
4. **Prueba el enrutamiento:** Navega por la aplicación, ve al dashboard u otras páginas del sistema, luego refresca la página (F5) en el navegador. Debería recargarse perfectamente sin dar ningún error visual.

---

> **Siguiente paso:** [05_MANTENIMIENTO_Y_APAGADO.md](./05_MANTENIMIENTO_Y_APAGADO.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
