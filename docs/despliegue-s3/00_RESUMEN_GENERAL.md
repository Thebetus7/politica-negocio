# Despliegue del Backend (Spring Boot + MongoDB) en AWS con S3 — Resumen General

Esta guía detalla paso a paso cómo configurar y desplegar el backend de **Spring Boot** en AWS utilizando un bucket de **AWS S3** privado para el almacenamiento de archivos (en reemplazo del contenedor de MinIO de desarrollo local) y cómo orquestar el backend y la base de datos **MongoDB** en una máquina virtual **AWS EC2** mediante **Docker**.

---

## 📋 Flujo General de Despliegue

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. CONFIGURACIÓN DE CREDENCIALES (IAM)                              │
│    Crear usuario programático y asignarle la política para S3       │
│                              ↓                                      │
│ 2. CONFIGURAR ALMACENAMIENTO BACKEND (SPRING BOOT)                  │
│    Crear bucket de S3 privado, CORS y configurar property/env       │
│                              ↓                                      │
│ 3. CREAR INSTANCIA EC2                                              │
│    Configurar e iniciar la máquina virtual en AWS                   │
│                              ↓                                      │
│ 4. INSTALAR HERRAMIENTAS EN EL SERVIDOR                             │
│    Conectarse por SSH, instalar Git, Docker, Buildx y Compose       │
│                              ↓                                      │
│ 5. SUBIR CÓDIGO Y DESPLEGAR CON DOCKER                              │
│    Clonar el proyecto, levantar contenedores y verificar logs       │
│                              ↓                                      │
│ 6. MANTENIMIENTO Y APAGADO DE RECURSOS                              │
│    Apagar la EC2, vaciar/eliminar bucket S3 y limpiar credenciales  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura de esta Guía

| Archivo | Contenido |
|---|---|
| `00_RESUMEN_GENERAL.md` | **Este archivo.** Flujo general de despliegue y estructura de la documentación del backend. |
| `01_CREACION_IAM.md` | Creación de credenciales y asignación de políticas de acceso seguro en AWS IAM. |
| `02_S3_ALMACENAMIENTO_BACKEND.md` | Crear el bucket privado, configurar CORS e integrar Spring Boot con AWS S3 en producción. |
| `03_CREAR_INSTANCIA_EC2.md` | Creación y configuración de la máquina virtual (EC2) y las reglas del firewall. |
| `04_INSTALAR_HERRAMIENTAS.md` | Pasos para conectarse por SSH e instalar Docker, Git y Docker Compose en la EC2. |
| `05_DESPLIEGUE_DOCKER.md` | Clonar el repositorio en el servidor, configurar variables y levantar Spring Boot y MongoDB. |
| `06_MANTENIMIENTO_Y_APAGADO.md` | Detener/terminar la EC2, vaciar el bucket y eliminar recursos de AWS para evitar cobros. |

---

> **Siguiente paso:** Continúa con [01_CREACION_IAM.md](./01_CREACION_IAM.md)
