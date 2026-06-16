# Despliegue Backend (Spring Boot + MongoDB) en AWS - SIN DOCKER (Vanilla)

Este enfoque instala todas las dependencias directamente en el sistema operativo del servidor AWS (EC2) usando **Amazon Linux 2023**. Es el método tradicional y requiere ejecutar comandos directamente en el sistema.

## 1. Crear el Servidor EC2 (Consola Web de AWS)
Sigue esta configuración paso a paso en el asistente de creación de instancias de AWS:

1. **Nombre y etiquetas:** Ponle un nombre identificativo (ej. `politica-backend`).
2. **Aplicación e imagen de sistema operativo (AMI):** Selecciona **Amazon Linux 2023 AMI** (es apto para la capa gratuita).
3. **Tipo de instancia:** Selecciona `t2.micro` (o `t3.micro` según tu región) para mantenerte dentro del límite gratuito.
4. **Par de claves (inicio de sesión):**
   * Haz clic en **"Crear un nuevo par de claves"** (si no tienes una).
   * Llámalo `politica-negocio-key`.
   * Tipo de clave: **RSA**. Formato: **`.pem`**.
   * Presiona **Crear**. En ese preciso momento, tu navegador descargará el archivo `politica-negocio-key.pem` a tu computadora. **¡Guárdalo bien!** Es el único momento donde AWS te lo dará.
5. **Configuraciones de red:**
   * Deja la red por defecto.
   * **Asignación automática de IP pública:** Asegúrate de que esté configurado en **"Habilitar"**.
   * **Firewall (grupos de seguridad):** Selecciona **"Crear un grupo de seguridad"**.
   * Marca la casilla **"Permitir el tráfico de SSH desde"** y selecciona **"Cualquier lugar (0.0.0.0/0)"**.
   * *(Importante para el backend)*: Haz clic en el botón **"Editar"** arriba a la derecha de esta sección de red. Baja hasta el final de las reglas y haz clic en **"Agregar regla de grupo de seguridad"**. Configura la regla con:
     * **Tipo:** TCP personalizado
     * **Intervalo de puertos:** `8081`
     * **Origen:** Cualquier lugar (`0.0.0.0/0`)
     * **Descripción:** `Backend Spring Boot`
6. **Configurar almacenamiento:** Deja el disco en **`8 GiB` gp3** (configuración por defecto).
7. **Detalles avanzados:**
   * **No toques absolutamente nada de esta sección.** Deja todos los selectores como vienen por defecto ("Seleccionar", "Ninguno", etc.).
   * El cuadro de texto final **"Datos de usuario - opcional"** debe quedar **completamente vacío**.
8. Haz clic en el botón naranja **"Lanzar instancia"** en el panel derecho.

## 2. Conectarse al Servidor vía SSH (En tu PC Local - MINGW64 / Git Bash)

> **Suposición:** Ya tienes la llave `politica-negocio-key.pem` guardada en tu carpeta `.ssh` y abriste **Git Bash directamente desde esa carpeta** (clic derecho en la carpeta `.ssh` → *Open Git Bash here*). Tu prompt ya muestra:
> ```
> USUARIO@BetoIlusion MINGW64 ~/.ssh
> $
> ```

1. Protege los permisos de la llave (solo lectura — solo la primera vez):
   ```bash
   chmod 400 politica-negocio-key.pem
   ```
2. Conéctate al servidor (reemplaza `IP_AWS` por la IP pública de tu EC2):
   ```bash
   ssh -i politica-negocio-key.pem ec2-user@IP_AWS
   ```
3. La primera vez te preguntará:
   > *Are you sure you want to continue connecting (yes/no)?*

   Escribe **`yes`** y dale Enter.

4. Si todo salió bien, tu terminal cambiará y mostrará:
   ```
   [ec2-user@ip-xxx-xxx-xxx-xxx ~]$
   ```
   Eso significa que ya estás dentro del servidor de AWS.

## 3. Instalar Java y MongoDB (En el Servidor AWS - Linux Amazon Linux 2023)
Ahora que estás dentro de la EC2, los comandos que ejecutes aquí afectarán solo al servidor de AWS. En Amazon Linux se usa el gestor de paquetes `dnf` (sucesor de `yum`):

```bash
# Se ejecuta en Linux (AWS) - Actualiza los paquetes del servidor
sudo dnf update -y

# Se ejecuta en Linux (AWS) - Instala Java 17 (Amazon Corretto)
sudo dnf install java-17-amazon-corretto-devel -y

# Se ejecuta en Linux (AWS) - Configurar el repositorio oficial para instalar MongoDB en Amazon Linux 2023
sudo tee /etc/yum.repos.d/mongodb-org-7.0.repo <<EOF
[mongodb-org-7.0]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/amazon/2023/mongodb-org/7.0/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-7.0.asc
EOF

# Instalar MongoDB
sudo dnf install -y mongodb-org

# Iniciar MongoDB y habilitarlo para que arranque con el sistema
sudo systemctl enable mongod
sudo systemctl start mongod
```

## 4. Configurar CORS y Compilar el Proyecto (En tu PC Local)

⚠️ **¡IMPORTANTE ANTES DE COMPILAR!** 
Para que tu frontend en producción (Angular) pueda comunicarse con este backend sin recibir el error de CORS (`0 Unknown Error`), debes configurar los permisos de origen.
1. Abre el archivo `src/main/java/com/example/politica_negocio/config/security/SecurityConfig.java`.
2. Busca el método `setAllowedOriginPatterns` y asegúrate de agregar `"*"` (o la IP pública de tu frontend) a la lista:
   ```java
   configuration.setAllowedOriginPatterns(java.util.List.of(
           "http://localhost:4201",
           "http://localhost:4200",
           "*" // <- ¡Permite peticiones desde la nube!
   ));
   ```
   *(Guarda el archivo antes de continuar).*

Abre **otra ventana** de terminal (PowerShell) en tu computadora local, navega a la carpeta `politica-negocio` y compila tu código Java:
```powershell
# Se ejecuta en PowerShell (Local). Usamos mvnw.cmd porque estás en Windows.
.\mvnw.cmd clean package -DskipTests
```
Esto genera un archivo llamado `politica-negocio-0.0.1-SNAPSHOT.jar` dentro de la subcarpeta `target`.

## 5. Subir el JAR al Servidor AWS (En tu PC Local - Git Bash / PowerShell)
En la misma terminal local donde compilaste, usa el comando Seguro de Copia (`scp`) apuntando a tu llave en la ruta segura de tu usuario:
```bash
# Se ejecuta en tu PC Local.
scp -i ~/.ssh/politica-negocio-key.pem target/politica-negocio-0.0.1-SNAPSHOT.jar ec2-user@IP_AWS:/home/ec2-user/
```

## 6. Ejecutar la Aplicación en AWS (En el Servidor AWS - Linux Amazon Linux 2023)
Vuelve a la primera terminal (donde estabas conectado por SSH a la EC2) y ejecuta la aplicación:
```bash
# Se ejecuta en Linux (AWS).
# "nohup" y el "&" final hacen que el programa siga corriendo incluso si cierras la terminal.
nohup java -jar politica-negocio-0.0.1-SNAPSHOT.jar > logs.txt 2>&1 &
```

---

## 🔄 ¿Cómo subir actualizaciones (si haces cambios en el código)?
Si modificas el código de tu Spring Boot localmente y quieres reflejar los cambios en el servidor, sigue este orden:

1. **Compilar de nuevo localmente (En tu PC Local - PowerShell):**
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   ```
2. **Apagar el backend anterior en el servidor (En el Servidor AWS - Linux Amazon Linux 2023):**
   ```bash
   # Busca el ID del proceso (PID) de Java corriendo en el fondo:
   ps aux | grep java
   
   # Mátalo con su número identificador (ejemplo: si el PID es 24102):
   kill 24102
   ```
3. **Subir el nuevo archivo compilado (En tu PC Local - Git Bash / PowerShell):**
   ```bash
   scp -i ~/.ssh/politica-negocio-key.pem target/politica-negocio-0.0.1-SNAPSHOT.jar ec2-user@IP_AWS:/home/ec2-user/
   ```
4. **Volver a encender en el fondo (En el Servidor AWS - Linux Amazon Linux 2023):**
   ```bash
   nohup java -jar politica-negocio-0.0.1-SNAPSHOT.jar > logs.txt 2>&1 &
   ```

---
### ⚖️ Diferencias / Comparativa
* **Ventajas:** Menos capas de abstracción, acceso directo al sistema operativo, ideal para máquinas con RAM muy limitada.
* **Desventajas:** Si cambias de servidor, tienes que volver a instalar Java, Mongo, configurar repositorios, etc., todo manualmente. Si actualizas a Java 21, tienes que desinstalar e instalar en el servidor.

---

## 🛑 ¿Cómo apagar servicios para ahorrar facturación?

Si vas a dejar de usar el backend por unos días y quieres evitar cargos en AWS (especialmente si se vence tu capa gratuita):

### Opción A: Apagar los procesos internos en Linux (La máquina sigue encendida)
Esto detiene el consumo de recursos de la CPU del servidor, pero AWS te seguirá cobrando por las horas de uso de la instancia EC2.
1. **Detener el Backend (Java):**
   ```bash
   # Buscar el PID del proceso de Java
   ps aux | grep java
   
   # Detener el proceso (Reemplaza 27446 por el PID real)
   kill 27446
   ```
2. **Detener MongoDB:**
   ```bash
   sudo systemctl stop mongod
   ```

### Opción B: Detener la Instancia EC2 completa (Recomendado para ahorrar dinero)
Apagar el servidor completo detiene los cargos de computación de la EC2 (solo se te cobrará unos centavos por el almacenamiento del disco virtual EBS).
1. Entra a la **Consola de AWS** -> **EC2** -> **Instancias**.
2. Selecciona tu instancia `politica-backend`.
3. Haz clic en **Estado de la instancia** -> **Detener instancia** (Stop instance). *¡NUNCA le des a Terminar instancia (Terminate) porque eso la borrará permanentemente!*

---

## 🚀 ¿Cómo volver a encender los servicios?

### Si detuviste la Instancia EC2 completa (Opción B)
1. Entra a la **Consola de AWS** -> **EC2** -> **Instancias**.
2. Selecciona tu instancia y haz clic en **Estado de la instancia** -> **Iniciar instancia** (Start instance).
3. **IMPORTANTE:** Cuando apagas y enciendes una EC2, **su IP pública cambia**. Deberás actualizar la nueva IP en el archivo `api-config.ts` de tu frontend y en el `api_client.dart` de tu aplicación móvil.
4. Conéctate nuevamente por SSH usando la nueva IP desde Git Bash en la carpeta `.ssh`:
   ```bash
   ssh -i politica-negocio-key.pem ec2-user@NUEVA_IP_AWS
   ```
5. **Iniciar MongoDB** (si no arranca automáticamente):
   ```bash
   sudo systemctl start mongod
   ```
6. **Volver a lanzar el Backend (Java):**
   ```bash
   nohup java -jar politica-negocio-0.0.1-SNAPSHOT.jar > logs.txt 2>&1 &
   ```

### Si solo detuviste los procesos dentro del Linux (Opción A)
Si no apagaste el servidor entero, la IP sigue siendo la misma:
1. Conéctate a la EC2 por SSH.
2. Inicia MongoDB:
   ```bash
   sudo systemctl start mongod
   ```
3. Ejecuta el JAR:
   ```bash
   nohup java -jar politica-negocio-0.0.1-SNAPSHOT.jar > logs.txt 2>&1 &
   ```
