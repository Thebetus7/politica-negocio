# Paso 6: Mantenimiento, Limpieza y Apagado (Evitar Cobros de AWS)

Para evitar cargos sorpresa en tu factura de AWS una vez que termines de evaluar el despliegue, o si necesitas reiniciar tu infraestructura desde cero, es fundamental apagar y eliminar correctamente todos los recursos creados, incluyendo la máquina virtual EC2.

---

## 6.1 — La Capa Gratuita en AWS (EC2 & S3)

AWS provee recursos de capa gratuita (Free Tier) durante los primeros 12 meses:
* **Amazon EC2:** 750 horas al mes de uso de instancias Linux `t2.micro` o `t3.micro`.
* **Amazon S3:** 5 GB de almacenamiento estándar, 20,000 solicitudes de lectura (GET) y 2,000 de escritura (PUT) al mes.

A pesar de esto, si dejas la instancia EC2 encendida tras pasar el año gratuito, u olvidas buckets llenos de datos antiguos, eventualmente podrías recibir cobros.

---

## 6.2 — Paso a Paso: Apagar o Terminar la Instancia EC2

Tienes dos opciones para apagar tu servidor en AWS:

### Opción A: Detener la Instancia (Stop) — Suspensión Temporal
* *¿Cuándo usarla?* Si deseas detener el cobro por hora de cómputo pero quieres **conservar el código, la base de datos de MongoDB y tus archivos** para usarlos otro día.
* **Pasos:**
  1. Ve a la consola de **EC2** -> **Instances** (Instancias).
  2. Selecciona la casilla de tu servidor `politica-backend-server`.
  3. Haz clic en el botón superior **Instance state** (Estado de la instancia) -> **Stop instance** (Detener instancia).
  4. El estado de la instancia cambiará a *Stopping* y luego a *Stopped*. *(AWS detendrá los cargos de computación, pero se mantendrá un cargo minúsculo por el almacenamiento de disco de 8 GiB).*

### Opción B: Terminar la Instancia (Terminate) — Borrado Definitivo
* *¿Cuándo usarla?* Si ya terminaste la evaluación del proyecto y deseas **eliminar por completo** el servidor y su disco duro virtual, liberando todo para evitar cualquier cobro.
* **Pasos:**
  1. Ve a la consola de **EC2** -> **Instances**.
  2. Selecciona la casilla de tu servidor `politica-backend-server`.
  3. Haz clic en **Instance state** -> **Terminate instance** (Terminar instancia).
  4. Confirma haciendo clic en **Terminate**. El estado cambiará a *Shutting-down* y luego a *Terminated*. La máquina y su disco se destruirán de forma definitiva.

---

## 6.3 — Paso a Paso: Vaciar y Eliminar el Bucket en S3

Un bucket de S3 no puede ser de baja si tiene archivos dentro. Debes vaciarlo primero:

### Paso A: Vaciar el Bucket
1. Ve al servicio de **S3** y haz clic en el nombre de tu bucket (ej. `politica-docs-prod`).
2. Haz clic en el botón **Empty** (Vaciar) ubicado en el menú superior derecho.
3. Escribe `permanently delete` en el campo de texto para confirmar que deseas borrar de forma definitiva todos los archivos.
4. Haz clic en **Empty** (Vaciar).

### Paso B: Eliminar el Bucket
1. Vuelve a la lista de buckets de **S3**.
2. Selecciona el bucket vacío marcando su casilla correspondiente.
3. Haz clic en el botón **Delete** (Eliminar).
4. Escribe el nombre exacto del bucket (ej. `politica-docs-prod`) en el campo de texto de confirmación.
5. Haz clic en **Delete bucket** (Eliminar bucket).

---

## 6.4 — Paso a Paso: Eliminar Credenciales y Usuario en IAM

Para garantizar la seguridad de tu infraestructura, nunca dejes usuarios programáticos inactivos:

1. Ve al servicio de **IAM** en la consola de AWS.
2. En el menú lateral izquierdo, haz clic en **Users** (Usuarios).
3. Selecciona la casilla del usuario `politica-negocio-s3-user`.
4. Haz clic en **Delete** (Eliminar) en el menú superior.
5. Escribe el nombre del usuario para confirmar y presiona **Delete**.
6. Ve a **Policies** (Políticas) en el menú izquierdo.
7. Busca tu política personalizada: `politica-negocio-s3-policy`.
8. Selecciónala, haz clic en **Policy actions** -> **Delete** y confirma la eliminación.

¡Listo! Con estos pasos, todos los recursos de AWS (EC2, S3 e IAM) han sido eliminados de forma limpia, y tu cuenta no generará cargos futuros.

---

> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
