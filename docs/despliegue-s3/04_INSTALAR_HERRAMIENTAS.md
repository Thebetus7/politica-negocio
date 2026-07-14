# Paso 4: Conectarse por Consola e Instar Herramientas en AWS EC2

Una vez que la instancia EC2 está encendida en AWS, debemos conectarnos a ella mediante la terminal (SSH) de nuestra computadora para instalar Git, Docker y Docker Compose.

---

## 4.1 — Conectarse al Servidor por SSH

Para acceder a la consola del servidor AWS en Windows:

1. Abre **PowerShell** o el **Símbolo del sistema (CMD)** en tu PC local.
2. Navega con la terminal a la carpeta exacta donde descargaste el archivo de clave `.pem` (por ejemplo, en Descargas o Documentos):
   ```powershell
   cd C:\Ruta\A\Tu\Clave
   ```
3. Ejecuta el comando de conexión SSH. Reemplaza `TU_IP_PUBLICA_EC2` por la IP que copiaste en el Paso 3:
   ```bash
   ssh -i politica-backend-key.pem ec2-user@TU_IP_PUBLICA_EC2
   ```
4. Si la terminal te pregunta si confías en la conexión (`Are you sure you want to continue connecting?`), escribe **`yes`** y presiona **Enter**.
5. Si la conexión fue exitosa, verás el prompt del servidor Linux en tu terminal:
   ```text
   [ec2-user@ip-172-31-41-220 ~]$
   ```
   *¡Ya estás conectado por consola a tu servidor en AWS! Cualquier comando que ejecutes a partir de aquí correrá en la nube.*

---

## 4.2 — Instalar Docker y Git

Dentro de la sesión SSH del servidor, ejecuta los siguientes comandos en orden:

```bash
# 1. Actualizar los paquetes del sistema operativo
sudo dnf update -y

# 2. Instalar Docker y Git
sudo dnf install docker git -y

# 3. Iniciar el servicio de Docker y configurarlo para que inicie automáticamente con el servidor
sudo systemctl start docker
sudo systemctl enable docker

# 4. Dar permisos al usuario ec2-user para ejecutar Docker sin requerir 'sudo'
sudo usermod -aG docker ec2-user
```

> [!IMPORTANT]
> Para aplicar el cambio de grupo de Docker a tu sesión actual, debes cerrar la conexión y volver a entrar, o simplemente ejecutar el siguiente comando dentro del servidor:
> ```bash
> newgrp docker
> ```

---

## 4.3 — Instalar Docker Buildx (Obligatorio en Amazon Linux 2023)

El instalador por defecto de Docker en Amazon Linux 2023 trae una versión antigua de Buildx. Sin embargo, Docker Compose moderno requiere **Buildx 0.17.0 o superior** para empaquetar imágenes. Debes instalarlo manualmente con estos comandos:

```bash
# 1. Crear el directorio de plugins de Docker
sudo mkdir -p /usr/local/lib/docker/cli-plugins

# 2. Descargar la última versión estable de Buildx (v0.19.3)
sudo curl -L "https://github.com/docker/buildx/releases/download/v0.19.3/buildx-v0.19.3.linux-amd64" \
  -o /usr/local/lib/docker/cli-plugins/docker-buildx

# 3. Dar permisos de ejecución al plugin
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx
```

### Verificar la versión de Buildx:
```bash
docker buildx version
```
*Debe responder `github.com/docker/buildx v0.19.3` o superior. Si te muestra una versión menor (como `0.12.1`), fuerza al perfil del usuario a usar el nuevo plugin con:*

```bash
echo 'export DOCKER_CLI_PLUGIN_EXTRA_DIRS=/usr/local/lib/docker/cli-plugins' >> ~/.bashrc
source ~/.bashrc
docker buildx version
```

---

## 4.4 — Instalar Docker Compose

Instalaremos la herramienta de orquestación para poder correr el backend y MongoDB juntos:

```bash
# 1. Descargar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose

# 2. Dar permisos de ejecución
sudo chmod +x /usr/local/bin/docker-compose
```

### Verificar instalación completa:
Ejecuta los siguientes comandos y comprueba que responden correctamente sin errores:
```bash
docker --version
docker-compose --version
docker buildx version
```

Si los tres comandos responden con sus versiones correctas, tu servidor en la nube ya está 100% equipado para desplegar el backend.

---

> **Siguiente paso:** [05_DESPLIEGUE_DOCKER.md](./05_DESPLIEGUE_DOCKER.md)  
> **Volver al índice:** [00_RESUMEN_GENERAL.md](./00_RESUMEN_GENERAL.md)
