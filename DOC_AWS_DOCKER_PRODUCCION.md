# Despliegue en AWS con Docker — `Política de Negocio`

Esta guía te explicará desde cero cómo tomar tu proyecto completo (Backend en Spring Boot, Frontend en Angular y Base de Datos MongoDB) y desplegarlo en un servidor en la nube de AWS utilizando **Docker y Docker Compose**.

Está pensada para alguien que **nunca ha hecho esto antes**. Al final de esta guía, tendrás tu aplicación funcionando en internet, accesible a través de una dirección IP pública.

A diferencia de levantar cada cosa por separado, usaremos **Docker Compose** para empaquetar el front, el back y la base de datos "juntos" de forma ordenada y profesional.

---

## Índice

0. [¿Qué es Docker y por qué lo usamos?](#0-qué-es-docker)
1. [Preparar tu proyecto local (Crear los Dockerfiles)](#1-preparar-tu-proyecto-local)
2. [Fase AWS: Crear el Servidor (EC2)](#2-fase-aws-crear-el-servidor-ec2)
3. [Conectarse a la EC2 e Instalar Docker](#3-conectarse-a-la-ec2-e-instalar-docker)
4. [Subir el código al servidor](#4-subir-el-código-al-servidor)
5. [Levantar el proyecto en Producción](#5-levantar-el-proyecto-en-producción)

---

## 0. ¿Qué es Docker y por qué lo usamos?

Imagina que tu aplicación es una receta. En tu computadora funciona porque tienes exactamente los ingredientes (Java 17, Node.js, MongoDB). Si llevas tu código a otro lado, podría fallar porque le faltan ingredientes.

**Docker** mete tu código junto con todos sus ingredientes dentro de una caja sellada llamada **Contenedor**. Ese contenedor funciona igual en tu PC, en la PC de un amigo, o en AWS. 
**Docker Compose** es el director de orquesta que le dice a Docker: *"Levanta la caja de la base de datos primero, luego la del backend, y finalmente la del frontend, y conéctalas entre sí"*.

---

## 1. Preparar tu proyecto local

Antes de ir a AWS, necesitamos crear 3 archivos en tu computadora para decirle a Docker cómo construir estas "cajas".

### 1.1 Dockerfile para el Backend (Spring Boot)
En la carpeta `politica-negocio`, crea un archivo llamado **exactamente** `Dockerfile` (sin extensión .txt ni nada) y pega esto:

```dockerfile
# 1. Etapa de construcción (Compilar el .jar)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Damos permisos al ejecutable de maven y compilamos
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# 2. Etapa de ejecución (Correr el .jar)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos solo el .jar generado en el paso anterior
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 1.2 Dockerfile para el Frontend (Angular)
En la carpeta `politica-negocio-frontend`, crea un archivo llamado `Dockerfile` y pega esto:

```dockerfile
# 1. Etapa de construcción (Compilar Angular a HTML/JS)
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 2. Etapa de servidor web (Servir los archivos estáticos con Nginx)
FROM nginx:alpine
# Nota: Si tu Angular es v17+, la ruta suele incluir /browser al final.
# Revisa la carpeta "dist" en tu local para confirmar la ruta exacta.
COPY --from=builder /app/dist/politica-negocio-frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```
*(Importante: En Angular, debes asegurarte de que tu `environment.ts` de producción apunte a la IP o dominio de tu servidor, no a localhost).*

### 1.3 El archivo Maestro: `docker-compose.yml`
En la **raíz de tu workspace** (fuera de las carpetas, justo donde ves ambas carpetas juntas), crea un archivo llamado `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:latest
    container_name: politica_mongodb
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    restart: always

  backend:
    build: 
      context: ./politica-negocio
      dockerfile: Dockerfile
    container_name: politica_backend
    ports:
      - "8081:8081"
    environment:
      # Conecta el backend al contenedor de mongo, no a localhost
      - spring.data.mongodb.uri=mongodb://mongodb:27017/politica_db
    depends_on:
      - mongodb
    restart: always

  frontend:
    build:
      context: ./politica-negocio-frontend
      dockerfile: Dockerfile
    container_name: politica_frontend
    ports:
      - "80:80" # El front estará en el puerto por defecto de internet
    depends_on:
      - backend
    restart: always

volumes:
  mongo_data:
```

---

## 2. Fase AWS: Crear el Servidor (EC2)

Ahora vamos a alquilar una computadora virtual en AWS.

1. Entra a tu consola de AWS y busca el servicio **EC2**.
2. Dale al botón naranja **"Launch Instance"** (Lanzar instancia).
3. **Nombre:** Ponle `Politica-Produccion`.
4. **Sistema Operativo (AMI):** Selecciona **Ubuntu** (elige la versión *Ubuntu 24.04 LTS* o *22.04 LTS*). Es más amigable para instalar Docker.
5. **Tipo de instancia:** Elige `t2.micro` o `t3.micro` (son gratis si tienes la capa gratuita, *Free Tier*).
6. **Key Pair (Par de claves):** 
   - Dale a "Create new key pair".
   - Nombre: `clave-politica`.
   - Tipo: RSA, Formato: `.pem`.
   - Al darle crear, se descargará un archivo a tu PC. **Guárdalo bien**, es la llave de tu servidor.
7. **Network settings (Configuración de red):**
   - Habilita (marca) las casillas: **Allow SSH traffic** (Puerto 22), **Allow HTTP traffic** (Puerto 80) y **Allow HTTPS traffic** (Puerto 443).
   - *Importante:* Dale al botón "Edit" en la red, y añade una regla extra (**Custom TCP**) para el puerto **8081** (para que tu frontend pueda hablar con tu backend).
8. **Almacenamiento:** Con 8 GB a 15 GB es suficiente.
9. Dale a **"Launch instance"**.

---

## 3. Conectarse a la EC2 e Instalar Docker

Una vez que la instancia diga "Running", cópiate su **Dirección IPv4 Pública** (Ej: `54.234.12.98`).

### Conectarte usando la llave
Abre tu terminal en tu computadora (Git Bash si estás en Windows) en la carpeta donde descargaste tu `clave-politica.pem` y escribe:

```bash
# Darle permisos de seguridad a la llave (solo en Mac/Linux/Git Bash)
chmod 400 clave-politica.pem

# Conectarse (reemplaza IP_PUBLICA por tu IP de AWS)
ssh -i "clave-politica.pem" ubuntu@IP_PUBLICA
```
Te preguntará *Are you sure you want to continue connecting?* Escribe `yes` y dale enter. ¡Felicidades, ya estás dentro de la computadora de AWS!

### Instalar Docker en el servidor de AWS
Copia y pega estos comandos uno a uno en la terminal de tu EC2:

```bash
# Actualizar el sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
sudo apt install docker.io docker-compose-v2 -y

# Darle permisos a tu usuario para usar docker sin poner "sudo" todo el tiempo
sudo usermod -aG docker ubuntu

# Activar docker para que arranque si se reinicia el servidor
sudo systemctl enable docker
sudo systemctl start docker
```
*Tip: Después de correr `usermod`, debes salir del servidor escribiendo `exit` y volver a entrar con el comando `ssh` para que los permisos surtan efecto.*

---

## 4. Subir el código al servidor

El servidor de AWS está vacío. Tenemos que pasar nuestro código allá. Tienes dos formas, pero la más fácil y profesional es **usar Git / GitHub**:

1. Sube tu código (incluyendo los `Dockerfile` y `docker-compose.yml`) a un repositorio en tu GitHub (puede ser privado).
2. En la terminal de tu EC2 de AWS, clona tu repositorio:
   ```bash
   # Te pedirá tu usuario y un Personal Access Token (si es privado)
   git clone https://github.com/tu-usuario/tu-repo.git
   
   # Entra a la carpeta
   cd tu-repo
   ```

---

## 5. Levantar el proyecto en Producción

Ya estás en la carpeta de tu servidor que tiene el archivo `docker-compose.yml`. Ahora viene la magia. Solo necesitas un comando:

```bash
docker compose up -d --build
```

**¿Qué hace esto?**
- `--build`: Lee tus Dockerfiles, descarga Java, descarga Node.js, compila tu código Spring Boot, compila tu código Angular y empaqueta todo. Tomará unos minutos la primera vez.
- `-d`: Lo ejecuta en modo "detached", lo que significa que puedes cerrar tu terminal y la aplicación seguirá corriendo en AWS para siempre.

### 6. Probar que todo funciona

Una vez que termine, ve a tu navegador web favorito:

- **Frontend (La página web):** Escribe directamente tu IP Pública de AWS en el navegador: `http://TU_IP_PUBLICA` (No necesitas puerto porque usamos el 80 en Docker).
- **Backend (La API):** Puedes probar: `http://TU_IP_PUBLICA:8081/api/...`

### Errores comunes:
1. **El frontend no conecta con el backend:** Revisa el archivo de entorno en tu Angular (`environment.ts` o `environment.prod.ts`). Ahí debes cambiar `localhost:8081` por `http://TU_IP_PUBLICA:8081` antes de subir el código a GitHub y compilar.
2. **CORS:** Si el backend rechaza al frontend, debes asegurarte que tu backend de Spring Boot permite peticiones desde tu IP de AWS (revisar configuración de CORS en el backend).

¡Eso es todo! Tienes un sistema moderno desplegado en contenedores en AWS. 🚀
