# Despliegue del Proyecto en AWS con S3 — Resumen General

Esta guía detalla paso a paso cómo desplegar el frontend de **Angular** en **AWS S3** como un sitio web estático y cómo configurar el backend de **Spring Boot** para utilizar un bucket de **AWS S3** en producción, reemplazando el contenedor de MinIO que se utiliza en el entorno de desarrollo local.

---

## 📋 Flujo General de Despliegue con S3

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. CONFIGURACIÓN DE CREDENCIALES (IAM)                              │
│    Crear usuario programático y asignarle la política para S3       │
│                              ↓                                      │
│ 2. CONFIGURAR ALMACENAMIENTO BACKEND (SPRING BOOT)                  │
│    Crear bucket de S3 privado, CORS y configurar property/env       │
│                              ↓                                      │
│ 3. DESPLEGAR EL FRONTEND (ANGULAR) EN S3                            │
│    Compilar la SPA, crear bucket público y habilitar Web Hosting    │
│                              ↓                                      │
│ 4. CONFIGURAR DISTRIBUCIÓN CLOUDFRONT                               │
│    Habilitar HTTPS (SSL/TLS) y resolver el enrutamiento SPA (404s)  │
│                              ↓                                      │
│ 5. APAGAR Y LIMPIAR SERVICIOS (Evitar facturación)                  │
│    Vaciar buckets, eliminar recursos de AWS y desactivar políticas  │
│                              ↓                                      │
│ 6. DESPLEGAR BACKEND Y MONGODB CON DOCKER                           │
│    Dockerfile, docker-compose y orquestación completa               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura de esta Guía

| Archivo | Contenido |
|---|---|
| `00_RESUMEN_GENERAL.md` | **Este archivo.** Flujo general, arquitectura de S3 y estructura de la documentación. |
| `01_CREACION_IAM.md` | Creación de credenciales y asignación de políticas de acceso seguro en AWS IAM. |
| `02_S3_ALMACENAMIENTO_BACKEND.md` | Crear el bucket privado, configurar CORS e integrar Spring Boot con AWS S3 en producción. |
| `03_S3_HOSTING_FRONTEND.md` | Crear bucket público, habilitar Static Website Hosting y subir la aplicación de Angular. |
| `04_AWS_CLOUDFRONT.md` | Configurar CloudFront para asegurar el frontend con HTTPS y corregir el redireccionamiento SPA. |
| `05_MANTENIMIENTO_Y_APAGADO.md` | Procedimientos para limpiar buckets y desactivar recursos para evitar cargos de facturación. |
| `06_DESPLIEGUE_DOCKER.md` | Creación de Dockerfile y docker-compose para orquestar Spring Boot y MongoDB. |

---

> **Siguiente paso:** Continúa con [01_CREACION_IAM.md](./01_CREACION_IAM.md)
