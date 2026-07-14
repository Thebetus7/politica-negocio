# Paso 1: Configurar Credenciales y Permisos en AWS IAM

Para que nuestro backend de **Spring Boot** pueda comunicarse de manera segura con AWS S3 en producción, necesitamos crear un **Usuario IAM** con permisos restringidos exclusivamente al bucket de S3 que se utilizará para almacenar los documentos.

---

## 1.1 — ¿Por qué usar IAM?
En AWS nunca debemos usar las credenciales raíz (Root) para conectar aplicaciones. En su lugar, usamos **IAM (Identity and Access Management)** para aplicar el *principio de menor privilegio*, otorgando acceso programático limitado únicamente a las acciones de S3 requeridas por el backend.

---

## 1.2 — Paso a Paso: Crear la Política de Permisos S3

Primero crearemos la política (policy) que define exactamente qué se le permite hacer al usuario:

1. Inicia sesión en la **Consola de AWS**.
2. En la barra de búsqueda superior, escribe **IAM** y selecciona el servicio.
3. En el menú lateral izquierdo, haz clic en **Policies** (Políticas) y luego en el botón **Create policy** (Crear política).
4. Cambia la pestaña del editor a **JSON** y borra el contenido por defecto.
5. Pega la siguiente estructura JSON (reemplaza `politica-docs-prod` por el nombre real de tu bucket si decides cambiarlo):

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "S3BackendStorageAccess",
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject",
                "s3:ListBucket",
                "s3:GetBucketLocation"
            ],
            "Resource": [
                "arn:aws:s3:::politica-docs-prod",
                "arn:aws:s3:::politica-docs-prod/*"
            ]
        }
    ]
}
```

6. Haz clic en **Next** (Siguiente).
7. Nombra la política como: `politica-negocio-s3-policy`.
8. Agrega una descripción (opcional), como: *Permisos de S3 para el backend de Politica Negocio*.
9. Haz clic en **Create policy** (Crear política).

---

## 1.3 — Paso a Paso: Crear el Usuario IAM y Adjuntar la Política

Ahora crearemos el usuario y le asignaremos la política de permisos:

1. En el menú lateral de **IAM**, haz clic en **Users** (Usuarios) y presiona **Create user** (Crear usuario).
2. Asigna el nombre de usuario: `politica-negocio-s3-user`.
3. **IMPORTANTE:** Deja **desmarcada** la casilla *Provide user access to the AWS Management Console*. Este usuario solo requiere acceso de API (programático), no acceso visual a la consola de AWS. Haz clic en **Next**.
4. En **Permissions options** (Opciones de permisos), selecciona **Attach policies directly** (Asociar políticas directamente).
5. En la barra de búsqueda de políticas, escribe `politica-negocio-s3-policy` (la política creada en el paso 1.2), selecciónala marcando la casilla de verificación y haz clic en **Next**.
6. Revisa los detalles y haz clic en **Create user** (Crear usuario).

---

## 1.4 — Generar Claves de Acceso Programático (Access Keys)

Para que el backend se autentique ante AWS, necesitamos generar un par de credenciales (Access Key ID y Secret Access Key):

1. En la lista de usuarios de **IAM**, haz clic en el usuario que acabas de crear: `politica-negocio-s3-user`.
2. Ve a la pestaña **Security credentials** (Credenciales de seguridad).
3. Baja hasta la sección **Access keys** y haz clic en **Create access key** (Crear clave de acceso).
4. Elige el caso de uso **Application running outside AWS** (Aplicación que se ejecuta fuera de AWS). Haz clic en **Next**.
5. (Opcional) Asigna una etiqueta descriptiva como `Spring Boot backend storage`.
6. Haz clic en **Create access key**.
7. **ADVERTENCIA CRÍTICA:** Verás en pantalla tu **Access key** y tu **Secret access key**. 
   * Haz clic en **Download .csv file** para descargar el archivo con las credenciales.
   * Guarda este archivo en un lugar sumamente seguro (por ejemplo, en un gestor de contraseñas).
   * **Nunca más podrás volver a ver la Secret Access Key**. Si la pierdes, tendrás que eliminar la Access Key actual y crear una nueva.
   * **NUNCA subas estas claves a GitHub** en archivos de texto, commits o código duro.

---

> **Siguiente paso:** [02_S3_ALMACENAMIENTO_BACKEND.md](./02_S3_ALMACENAMIENTO_BACKEND.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
