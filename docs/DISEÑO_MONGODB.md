# Diseño de Base de Datos MongoDB para Sistema de Políticas de Negocio

El esquema de la base de datos se ha rediseñado para migrar de un modelo relacional estricto (SQL) a un modelo documental flexible (NoSQL - MongoDB). A continuación se documentan las decisiones de diseño adoptadas.

## Estrategia de Migración (Relacional -> Documental)

1. **Eliminación de Tablas Intermedias:** 
   En esquemas relacionales, colecciones como `FuncionarioDepa` o `Colaborador` se usaban para mapear relaciones muchos a muchos. En MongoDB, estas relaciones se modelan principalmente incrustando arreglos de ObjectIDs o subdocumentos directamente. Por ejemplo, en lugar de un `FuncionarioDepa`, el `Usuario` puede tener un campo `departamentoId`.
   
2. **Sistema Global de Auditoría (Timestamps y Soft Delete):**
   Dado que `Spring Data MongoDB` no soporta la anotación nativa `@SQLDelete` de JPA, se ha implementado un sistema a nivel de aplicación.
   Todas las colecciones extenderán una `BaseEntity` que incluye:
   - `createdAt`: Fecha de creación.
   - `updatedAt`: Fecha de última modificación.
   - `deletedAt`: Fecha de eliminación lógica. 

   > *Nota:* Cualquier query en la capa de acceso a datos (Repositorios) debe filtrar por `{ deletedAt: null }` para evitar retornar registros "eliminados".

## Colecciones Principales

### 1. Usuarios (`usuarios`)
Centraliza el acceso al sistema para todos los roles.
- `id` (ObjectId)
- `nombre` (String)
- `correo` (String)
- `contra` (String)
- `rol` (Enum: `ADMINISTRADOR`, `FUNCIONARIO`, `ATENCION_CLIENTE`)
- `departamentoId` (ObjectId) - Referencia al departamento si aplica.

### 2. Departamentos (`departamentos`)
- `id` (ObjectId)
- `nombre` (String)
- `descripcion` (String)

### 3. Políticas de Negocio (`politicas_negocio`)
Define la plantilla o flujo de negocio que ejecutará un cliente/usuario.
- `id` (ObjectId)
- `nombre` (String)
- `descripcion` (String)
- `flujoIds` (Array de ObjectId) - Lista de flujos u operaciones.
- `colaboradores` (Array de Objetos incrustados: `userId`, `estado`) - Reemplaza la tabla `Colaborador`.

### 4. Portafolios (`portafolios`)
El portafolio representa los "datos o contexto" del cliente real sobre el que el rol "Atención al Cliente" ejecutará la política de negocio.
- `id` (ObjectId)
- `clienteData` (JSON / Objeto incrustado) - Contiene los datos recopilados del cliente del mundo real.
- `politicaId` (ObjectId) - Qué política de negocio se va a aplicar a este portafolio.
- `atencionClienteId` (ObjectId) - Usuario AC responsable.

### 5. Diagramas y Formularios
(Adaptados para almacenar el esquema como JSON nativo)
- **`log_diagramas`**: Almacena el `json` y se relaciona con el esquema principal para auditoría.
- **`formularios`**: Guarda el esquema `json` de formularios interactivos para las actividades.

---
Con este enfoque, logramos un modelo de datos más ágil, reducimos los JOINs (lookups) y aprovechamos el poder del esquema dinámico en los Formularios y Log de Diagramas.
