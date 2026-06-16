# Despliegue Backend (Spring Boot + MongoDB) en AWS - CON DOCKER

Este enfoque empaqueta el backend y la base de datos en contenedores. No instalamos Java ni Mongo directamente en el sistema operativo del servidor. Usaremos **Amazon Linux 2023** en la EC2.

## 1. Crear el Servidor EC2 (Consola Web de AWS)
Sigue esta configuración paso a paso en el asistente de creación de instancias de AWS:

1. **Nombre y etiquetas:** Ponle un nombre identificativo (ej. `politica-backend-docker`).
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

## 3. Instalar Docker en la EC2 (En el Servidor AWS - Linux Amazon Linux 2023)
A diferencia del método Vanilla, aquí **no** instalaremos ni Java ni MongoDB. Solo instalaremos el motor de Docker usando el gestor de paquetes `dnf`:
```bash
# Se ejecuta en Linux (AWS) - Actualiza repositorios
sudo dnf update -y

# Se ejecuta en Linux (AWS) - Instalar Docker
sudo dnf install docker -y

# Activar docker para que arranque con el sistema
sudo systemctl enable docker
sudo systemctl start docker

# Permite que el usuario 'ec2-user' use docker sin escribir "sudo" siempre
sudo usermod -aG docker ec2-user

# Instalar Docker Compose (Descargando el ejecutable ejecutable directamente)
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```
*(Importante: Tras el comando `usermod`, debes escribir `exit` para salir del servidor y volver a entrar con el comando `ssh` para que el permiso del grupo `docker` se aplique).*

## 4. Preparar Código y Archivos (En tu PC Local - Editor de Código / IDE)

⚠️ **¡IMPORTANTE ANTES DE CONTINUAR! (Configurar CORS)** 
Para que tu frontend en producción pueda comunicarse con este backend sin recibir el error de CORS (`0 Unknown Error`), debes configurar los permisos de origen.
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

Ahora, abre tu proyecto localmente y crea dos archivos fundamentales para orquestar Docker.

En `politica-negocio`, creas un **`Dockerfile`**:
```dockerfile
# Instrucciones para que Docker compile Java internamente
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Instrucciones para que Docker corra el JAR compilado
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Y un **`docker-compose.yml`**:
```yaml
version: '3.8'
services:
  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
  backend:
    build: .
    ports:
      - "8081:8081"
    environment:
      # Conectamos SpringBoot al contenedor de Mongo (llamado 'mongodb')
      - spring.data.mongodb.uri=mongodb://mongodb:27017/politica_db
    depends_on:
      - mongodb
```

## 5. Subir y Desplegar (Terminal Local y luego Terminal AWS)
Primero, debes subir tu código. Puedes hacerlo subiéndolo a GitHub y clonándolo desde AWS, o copiándolo con SCP:
```bash
# Opción SCP: Se ejecuta en tu PC Local (PowerShell/Git Bash)
# Copiamos toda la carpeta de tu backend al servidor usando el usuario ec2-user y apuntando a tu llave segura en ~/.ssh/
scp -i ~/.ssh/politica-negocio-key.pem -r "ruta/a/tu/politica-negocio" ec2-user@IP_AWS:/home/ec2-user/
```

Una vez que el código esté en AWS, entra por SSH y lanza la magia:
```bash
# Se ejecuta en Linux (AWS)
cd politica-negocio

# Este comando lee el docker-compose.yml, levanta MongoDB, 
# compila Spring Boot y los conecta a ambos en segundo plano (-d)
# Nota: Usamos docker-compose con guión medio según lo instalado en el Paso 3
docker-compose up -d --build
```

---

## 🔄 ¿Cómo subir actualizaciones (si haces cambios en el código)?
Con Docker el proceso es sumamente limpio e indoloro:

1. **Subir los cambios de tu código (En tu PC Local):**
   * *Si usas Git:* Haces un `git push` local y en la consola de AWS (en la carpeta del proyecto) ejecutas `git pull`.
   * *Si usas SCP:* Vuelves a copiar tu carpeta de código modificada al servidor:
     ```bash
     scp -i ~/.ssh/politica-negocio-key.pem -r "ruta/a/tu/politica-negocio" ec2-user@IP_AWS:/home/ec2-user/
     ```
2. **Re-desplegar los contenedores (En el Servidor AWS - Linux Amazon Linux 2023):**
   Navega a la carpeta de tu proyecto en AWS y ejecuta el mismo comando:
   ```bash
   docker-compose up -d --build
   ```
   * **¿Por qué esto es mejor?** Docker Compose detectará automáticamente que el código del backend cambió. Procederá a detener y reconstruir únicamente el contenedor del backend (`politica_backend`). 
   * **MongoDB no se detendrá ni se alterará**, lo que significa que **no perderás tus datos** y la base de datos se mantendrá intacta.

---
### ⚖️ Diferencias / Comparativa
* **Ventajas:** Si migras de servidor, solo instalas Docker y corres 1 comando. El entorno (versión de Java, Alpine Linux) es idéntico sin importar dónde lo corras. No "ensucias" el servidor con instalaciones.
* **Desventajas:** Docker consume un poco más de memoria RAM (overhead) y espacio en disco. En instancias muy pequeñas (ej. t2.micro) construir la imagen a veces puede ser lento.

---

## 🛑 ¿Cómo apagar servicios para ahorrar facturación?

Si vas a dejar de usar el backend por unos días y quieres evitar cargos en AWS (especialmente si se vence tu capa gratuita):

### Opción A: Apagar los contenedores Docker (La máquina sigue encendida)
Esto libera la memoria RAM y detiene el procesamiento de Docker, pero AWS te seguirá cobrando por la instancia EC2 encendida.
1. Conéctate a tu servidor de AWS por SSH.
2. Entra a la carpeta de tu proyecto:
   ```bash
   cd politica-negocio
   ```
3. Detén todos los contenedores levantados por Compose:
   ```bash
   docker-compose down
   ```

### Opción B: Detener la Instancia EC2 completa (Recomendado para ahorrar dinero)
Apagar el servidor completo detiene los cargos de computación de la EC2 (solo se te cobrará unos centavos por el almacenamiento del disco virtual EBS).
1. Entra a la **Consola de AWS** -> **EC2** -> **Instancias**.
2. Selecciona tu instancia `politica-backend-docker`.
3. Haz clic en **Estado de la instancia** -> **Detener instancia** (Stop instance). *¡NUNCA la termines (Terminate)!*

---

## 🚀 ¿Cómo volver a encender los servicios?

### Si detuviste la Instancia EC2 completa (Opción B)
1. Entra a la **Consola de AWS** -> **EC2** -> **Instancias** y dale a **Iniciar instancia** (Start instance).
2. **IMPORTANTE:** Cuando apagas y enciendes una EC2, **su IP pública cambia**. Deberás actualizar la nueva IP en el archivo `api-config.ts` de tu frontend y en el `api_client.dart` de tu aplicación móvil.
3. Conéctate por SSH usando la nueva IP desde Git Bash en la carpeta `.ssh`:
   ```bash
   ssh -i politica-negocio-key.pem ec2-user@NUEVA_IP_AWS
   ```
4. Navega al proyecto y levanta Docker Compose:
   ```bash
   cd politica-negocio
   docker-compose up -d
   ```
   *(Como la base de datos se guarda en un volumen o directamente en el contenedor, tu data persistirá).*

### Si solo detuviste los contenedores (Opción A)
Si no apagaste el servidor entero, la IP sigue siendo la misma:
1. Conéctate a la EC2 por SSH.
2. Navega al proyecto y levanta de nuevo:
   ```bash
   cd politica-negocio
   docker-compose up -d
   ```
