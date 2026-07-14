# Paso 5: Mantenimiento, Limpieza y Apagado (Evitar Cobros de AWS)

Para evitar cargos sorpresa en tu factura de AWS una vez que termines de evaluar el despliegue, o si necesitas reiniciar tu infraestructura desde cero, es fundamental apagar y eliminar correctamente todos los recursos creados.

---

## 5.1 — La Capa Gratuita de AWS S3 y CloudFront

AWS provee recursos de capa gratuita (Free Tier) durante los primeros 12 meses:
* **Amazon S3:** 5 GB de almacenamiento estándar, 20,000 solicitudes de lectura (GET) y 2,000 de escritura (PUT) al mes.
* **AWS CloudFront:** 1 TB de transferencia de datos de salida de internet y 10,000,000 de solicitudes HTTP/HTTPS al mes de por vida.

A pesar de esto, si olvidas buckets llenos de datos antiguos, archivos versionados o distribuciones activas que ya no usas, eventualmente podrías recibir cobros.

---

## 5.2 — Paso a Paso: Eliminar la Distribución de CloudFront

No es posible eliminar una distribución de CloudFront de forma directa si está activa. Debes seguir este flujo:

1. Ve a la consola de **CloudFront**.
2. Selecciona la casilla al lado de tu distribución (`https://d12345.cloudfront.net`).
3. Haz clic en el botón **Disable** (Desactivar) en el menú superior.
4. Confirma la acción haciendo clic en **Disable** en la ventana emergente.
5. El estado cambiará a *Deploying* y luego a **Disabled** (Desactivado). Esto puede tomar de 2 a 5 minutos.
6. Una vez desactivada, selecciona nuevamente la casilla de la distribución y presiona el botón **Delete** (Eliminar).
7. Confirma la eliminación.

---

## 5.3 — Paso a Paso: Vaciar y Eliminar Buckets en S3

Un bucket de S3 no puede ser eliminado si tiene archivos dentro. Debes vaciarlo primero:

### Paso A: Vaciar el Bucket
1. Ve al servicio de **S3** y haz clic en el nombre de tu bucket (ej. `politica-frontend-prod` o `politica-docs-prod`).
2. Haz clic en el botón **Empty** (Vaciar) ubicado en el menú superior derecho.
3. Escribe `permanently delete` en el campo de texto para confirmar que deseas borrar de forma definitiva todos los archivos.
4. Haz clic en **Empty** (Vaciar).

### Paso B: Eliminar el Bucket
1. Vuelve a la lista de buckets de **S3**.
2. Selecciona el bucket vacío marcando su casilla correspondiente.
3. Haz clic en el botón **Delete** (Eliminar).
4. Escribe el nombre exacto del bucket (ej. `politica-frontend-prod`) en el campo de texto de confirmación.
5. Haz clic en **Delete bucket** (Eliminar bucket).

> *Repite estos dos pasos tanto para el bucket del frontend (`politica-frontend-prod`) como para el bucket del backend (`politica-docs-prod`).*

---

## 5.4 — Paso a Paso: Eliminar Credenciales y Usuario en IAM

Para garantizar la seguridad de tu infraestructura, nunca dejes usuarios programáticos inactivos:

1. Ve al servicio de **IAM** en la consola de AWS.
2. En el menú lateral izquierdo, haz clic en **Users** (Usuarios).
3. Selecciona la casilla del usuario `politica-negocio-s3-user`.
4. Haz clic en **Delete** (Eliminar) en el menú superior.
5. Escribe el nombre del usuario para confirmar y presiona **Delete**.
6. Ve a **Policies** (Políticas) en el menú izquierdo.
7. Busca tu política personalizada: `politica-negocio-s3-policy`.
8. Selecciónala, haz clic en **Policy actions** -> **Delete** y confirma la eliminación.

¡Listo! Con estos pasos, todos los recursos asociados al despliegue en S3 de AWS han sido eliminados de forma limpia, y tu cuenta no generará cargos futuros.

---

> **Siguiente paso:** [06_DESPLIEGUE_DOCKER.md](./06_DESPLIEGUE_DOCKER.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
