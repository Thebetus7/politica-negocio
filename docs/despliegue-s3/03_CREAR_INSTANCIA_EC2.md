# Paso 3: Crear la Instancia EC2 en AWS

Para desplegar nuestro backend empaquetado en Docker, necesitamos una máquina virtual en la nube. AWS ofrece esto a través del servicio **EC2 (Elastic Compute Cloud)**.

Sigue estos pasos en la consola de AWS para crear y configurar la máquina virtual:

---

## 3.1 — Lanzar una Instancia EC2

1. Busca y selecciona **EC2** en la barra de búsqueda de la consola de AWS.
2. En el panel de control de EC2, haz clic en el botón **Launch instance** (Lanzar instancia).
3. Configura los campos del formulario de creación:
   * **Name (Nombre):** `politica-backend-server`.
   * **Application and OS Images (AMI):** Selecciona **Amazon Linux 2023 AMI** (asegúrate de que tenga la etiqueta *Free tier eligible* / Apto para la capa gratuita).
   * **Instance type (Tipo de instancia):** Selecciona `t2.micro` (o `t3.micro` según disponibilidad y zona, ambas suelen entrar en la capa gratuita).
   * **Key pair (Par de claves):**
     * Haz clic en **Create new key pair** (Crear nuevo par de claves).
     * Nómbrala como `politica-backend-key`.
     * Tipo de par de claves: **RSA**.
     * Formato del archivo de clave privada: **`.pem`**.
     * Haz clic en **Create key pair**. Tu navegador descargará automáticamente el archivo `politica-backend-key.pem`. 
     * **ADVERTENCIA:** Guarda este archivo `.pem` en un lugar seguro de tu computadora (por ejemplo, en `C:\Users\USUARIO\.ssh\` o en tus documentos). Lo usarás en el siguiente paso para ingresar por consola SSH al servidor.

---

## 3.2 — Configurar la Red y Seguridad (Security Groups)

El grupo de seguridad actúa como un firewall virtual que controla el tráfico entrante y saliente del servidor.

1. En la sección **Network settings** (Configuración de red):
   * Deja marcada la opción **Create security group** (Crear grupo de seguridad).
   * **Allow SSH traffic from:** Selecciona **Anywhere (0.0.0.0/0)** para permitir conectarte por terminal desde tu PC local. *(Para mayor seguridad, puedes cambiarlo a "My IP" si tienes una IP pública estática)*.
2. Agrega una nueva regla de firewall para que los usuarios (y el frontend de Angular) puedan conectarse al backend en el puerto `8081`:
   * Haz clic en **Add security group rule** (o edítalo tras crear la instancia).
   * **Type (Tipo):** Custom TCP (TCP personalizado).
   * **Port range (Rango de puertos):** Escribe `8081` (puerto donde corre nuestro backend de Spring Boot).
   * **Source (Origen):** Selecciona **Anywhere-IPv4 (0.0.0.0/0)**.
3. Si en algún momento decides mapear el backend al puerto HTTP estándar de internet, puedes agregar también una regla para permitir tráfico **HTTP (puerto 80)**.

---

## 3.3 — Configurar el Almacenamiento

1. En la sección **Configure storage**:
   * Deja la configuración por defecto: **1 volume, 8 GiB, gp3**. 
   * *(La capa gratuita te permite configurar hasta 30 GiB si deseas ampliarlo, pero 8 GiB es más que suficiente para correr el backend y MongoDB en Docker).*

---

## 3.4 — Lanzar la Instancia

1. Revisa los detalles en el resumen lateral derecho.
2. Haz clic en el botón **Launch instance** (Lanzar instancia).
3. Espera a que la consola confirme la creación y haz clic en **View all instances** (Ver todas las instancias).
4. Verás tu instancia `politica-backend-server`. Cuando el estado de la instancia cambie a **Running** (En ejecución), la máquina estará lista.
5. **Copia la IP Pública:** Selecciona tu instancia en la tabla y busca abajo en la pestaña de detalles el campo **Public IPv4 address** (IP pública IPv4, por ejemplo `54.210.45.120`). Anota esta dirección IP ya que la necesitarás para conectarte.

---

> **Siguiente paso:** [04_INSTALAR_HERRAMIENTAS.md](./04_INSTALAR_HERRAMIENTAS.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
