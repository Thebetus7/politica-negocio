# Adaptación de Base de Datos Relacional a MongoDB

Este documento explica las decisiones tomadas para adaptar el esquema relacional original (XSD) a una base de datos documental (MongoDB) dentro del proyecto PolicyBusiness.

## 1. De Tablas a Colecciones
En lugar de tablas rígidas, hemos utilizado colecciones de documentos JSON/BSON. Esto permite una mayor flexibilidad en la estructura de los datos.

- **Usuario**: Se mantiene como una colección central. El campo `rol` se maneja como un Enum para facilitar la lógica de negocio.
- **PoliticaNegocio**: Al ser un sistema de diagramación, la política almacena el `diagramData` como un objeto embebido o JSON, permitiendo que GoJS recupere la estructura completa del diagrama sin necesidad de múltiples joins.
- **Portafolio**: El portafolio de clientes se asocia a un usuario y a una política, manteniendo la trazabilidad.

## 2. Implementación de Soft Delete
Para cumplir con el requerimiento de "Soft Delete", se ha añadido el campo `isDeleted` (booleano) en todas las entidades principales.
- Los repositorios de Spring Data MongoDB filtran lógicamente estos registros.
- Esto permite recuperar datos accidentalmente borrados y mantener la integridad referencial histórica.

## 3. Timestamps Automáticos
Se han incluido los campos:
- `createdAt`: Fecha de creación del registro.
- `updatedAt`: Fecha de la última modificación.
Esto se gestiona automáticamente mediante los listeners de Spring Data o asignaciones manuales en los servicios antes del `save()`.

## 4. Relaciones y Referencias
A diferencia de SQL, donde se usan llaves foráneas físicas:
- Usamos `@DocumentReference` o simplemente almacenamos el ID del documento relacionado.
- Esto reduce el acoplamiento y mejora el rendimiento de lectura en MongoDB.

## 5. Auditoría
La estructura actual permite expandir fácilmente a un sistema de auditoría completo, ya que cada documento lleva su propia estampa de tiempo y estado de eliminación.
