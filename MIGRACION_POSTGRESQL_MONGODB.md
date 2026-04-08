# Transición de PostgreSQL a MongoDB

## 1. Contexto General: ¿Por qué MongoDB?

### 📊 Comparativa Relacional vs No-Relacional

| Aspecto | PostgreSQL (Relacional) | MongoDB (Documento) |
|---------|------------------------|-------------------|
| **Modelo de Datos** | Tablas con filas y columnas rigidas | Colecciones de documentos JSON flexibles |
| **Schema** | ✅ Schema rígido (predefinido) | ❌ Schema flexible (se adapta) |
| **Relaciones** | `JOIN` entre tablas | Embedding o referencias entre documentos |
| **Validación** | Constraints en BD | Validación en aplicación |
| **Escalabilidad** | Vertical (mejorar servidor) | Horizontal (distribuir datos) |
| **Transacciones** | ACID múltiples tablas | ACID en documento único (v4+) |
| **Tipo de datos complejos** | ❌ Limitado | ✅ Arrays, objetos anidados nativos |

---

## 2. Estado Actual: PostgreSQL ➜ MongoDB (MIGRADO ✅)

### 2.1 Arquitectura Anterior (PostgreSQL)

```
┌─────────────────────────────────────────────────────┐
│          SPRING BOOT + PostgreSQL                    │
├─────────────────────────────────────────────────────┤
│  Entidades (3)                                       │
│  ├─ Usuario                                          │
│  ├─ PoliticaNegocio                                 │
│  └─ LogDiagrama                                     │
│                                                      │
│  Repositorios (2)                                    │
│  ├─ UsuarioRepository extends JpaRepository         │
│  └─ PoliticaNegocioRepository extends JpaRepository │
│                                                      │
│  ORM: Hibernate/JPA                                 │
│  Dialecto: PostgreSQL                               │
└─────────────────────────────────────────────────────┘
             ↓
     postgresql://localhost:5432/politica_db
```

### 2.2 Arquitecura Nueva (MongoDB - ACTUAL)

```
┌─────────────────────────────────────────────────────┐
│     SPRING BOOT + MongoDB (Spring Data MongoDB)     │
├─────────────────────────────────────────────────────┤
│  Documentos (3)                                      │
│  ├─ Usuario                                          │
│  ├─ PoliticaNegocio (con logs anidados)            │
│  └─ LogDiagrama (independiente)                     │
│                                                      │
│  Repositorios (3)                                    │
│  ├─ UsuarioRepository extends MongoRepository      │
│  ├─ PoliticaNegocioRepository extends MongoRepository
│  └─ LogDiagramaRepository extends MongoRepository   │
│                                                      │
│  Driver: MongoDB Java Driver                        │
│  Spring Data: spring-boot-starter-data-mongodb     │
└─────────────────────────────────────────────────────┘
             ↓
        mongodb://localhost:27017/politica_db
```

---

## 3. Guía: ¿Qué Necesito Hacer? (PASO A PASO)

### 🔧 PASO 1: Instalar MongoDB Localmente

Tienes **2 opciones**:

#### OPCIÓN A: Con Docker (Recomendado - Fácil)

Si tienes Docker instalado:

```bash
# 1. Descargar la imagen de MongoDB
docker pull mongo

# 2. Levantar MongoDB en puerto 27017
docker run -d -p 27017:27017 --name mongodb mongo:latest

# 3. Verificar que está corriendo
docker ps

# Terminal output esperado:
# CONTAINER ID   IMAGE     COMMAND                  PORTS
# abc123...      mongo     "docker-entrypoint.s…"   0.0.0.0:27017->27017/tcp   mongodb

# 4. Cuando termines y quieras detenerlo:
docker stop mongodb
docker rm mongodb
```

**Ventajas:**
- ✅ Aislado (no afecta tu sistema)
- ✅ Fácil de remover
- ✅ Reproducible en cualquier máquina

---

#### OPCIÓN B: Instalación Local (Windows)

1. Descargar MongoDB Community Edition:
   - URL: https://www.mongodb.com/try/download/community
   - Selecciona: Windows, .msi installer

2. Ejecutar el instalador (.msi)

3. MongoDB se instala como servicio de Windows automáticamente

4. Verificar que funciona:
   ```
   # En PowerShell o CMD
   mongosh
   
   # Deberías ver:
   > # (prompt de MongoDB)
   ```

**Ventajas:**
- ✅ Integrado con Windows
- ✅ Inicia automáticamente

---

### 🚀 PASO 2: Verificar Conexión a MongoDB

Una vez que MongoDB está corriendo:

```bash
# Opción 1: Con mongosh (herramienta oficial)
mongosh mongodb://localhost:27017

# Deberías ver:
# Current Mongosh Log ID: ...
# test>

# Opción 2: Testear desde Java (lo hace automáticamente Spring Boot)
```

---

### 🌐 PASO 3: Levantar la Aplicación Spring Boot

```bash
# Navega a la carpeta del proyecto
cd "c:\EDBERTO\ULT SEMESTRE\SW1\1ER parcial\SW1_PN_1_2026\politica-negocio"

# Ejecuta la aplicación con Maven
./mvnw.cmd spring-boot:run

# La aplicación debería mostrar:
# ============================
# Tomcat started on port(s): 8080 (http)
# ...
# ✅ Seed: Usuario admin creado.
# ✅ Seed: Política inicial creada.
```

**Qué sucede automáticamente:**
1. Spring Boot se conecta a `mongodb://localhost:27017/politica_db`
2. MongoDB crea la BD `politica_db` y las colecciones automáticamente
3. DatabaseSeeder ejecuta el seed de datos iniciales:
   - Usuario: `admin` / `admin123`
   - Política: `Política de Ventas Estándar`

---

### 🔍 PASO 4: Verificar que Datos Están en MongoDB

#### Opción A: Con MongoDB Compass (GUI Visual)

1. Descargar MongoDB Compass:
   - URL: https://www.mongodb.com/try/download/compass
   - Instalar

2. Abrir Compass

3. Conectar a: `mongodb://localhost:27017`

4. Explorar en el árbol izquierdo:
   ```
   politica_db
   ├─ usuarios
   │  └─ documento de admin
   └─ politicasNegocio
      └─ documento de política inicial
   ```

---

#### Opción B: Con mongosh (CLI)

```bash
# Conectar a MongoDB
mongosh mongodb://localhost:27017

# Una vez conectado, ejecutar:
test> use politica_db
politica_db> db.usuarios.find()
# Output esperado:
# [
#   {
#     _id: ObjectId("..."),
#     username: 'admin',
#     password: 'admin123'
#   }
# ]

# Ver políticas
politica_db> db.politicasNegocio.find()
# Output esperado:
# [
#   {
#     _id: ObjectId("..."),
#     nombre: 'Política de Ventas Estándar',
#     descripcion: 'Diagrama base para el proceso de ventas',
#     estado: 'borrador',
#     logs: []
#   }
# ]

# Salir
politica_db> exit
```

---

## 4. Estructura de Documentos MongoDB

### Colección: `usuarios`

```json
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "username": "admin",
  "password": "admin123"
}
```

**Campos:**
- `_id`: ObjectId único (generado automáticamente por MongoDB)
- `username`: String (validado: no puede estar vacío)
- `password`: String (validado: no puede estar vacío)

---

### Colección: `politicasNegocio`

```json
{
  "_id": ObjectId("507f191e810c19729de860ea"),
  "nombre": "Política de Ventas Estándar",
  "descripcion": "Diagrama base para el proceso de ventas",
  "estado": "borrador",
  "logs": [
    {
      "tiempo": ISODate("2026-04-08T11:00:00Z"),
      "json": "{...contenido del diagrama...}"
    },
    {
      "tiempo": ISODate("2026-04-08T12:00:00Z"),
      "json": "{...diagrama actualizado...}"
    }
  ]
}
```

**Nota Importante:**
- Los logs están **ANIDADOS** dentro de cada política (Embedding)
- Es decir: 1 documento política = múltiples logs dentro del mismo documento

---

## 5. Cambios en el Código (Qué Cambió)

### 5.1 Dependencias Reemplazadas

**ANTES:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

**AHORA:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

### 5.2 Configuración application.properties

**ANTES:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/politica_db
spring.datasource.username=postgres
spring.datasource.password=123456789
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
```

**AHORA:**
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/politica_db
spring.data.mongodb.auto-index-creation=true
```

---

### 5.3 Entidades: De JPA a MongoDB

#### USUARIO.java

**ANTES:**
```java
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String username;
}
```

**AHORA:**
```java
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;  // MongoDB genera automáticamente
    
    @NotBlank
    private String username;
}
```

---

#### POLITICANEGOCIO.java

**ANTES:**
```java
@Entity
@Table(name = "politica_negocio")
public class PoliticaNegocio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String descripcion;
    private String estado;
    // Sin relación con logs
}
```

**AHORA:**
```java
@Document(collection = "politicasNegocio")
public class PoliticaNegocio {
    @Id
    private String id;
    
    @NotBlank
    private String nombre;
    
    private String descripcion;
    
    @NotBlank
    private String estado;
    
    private List<LogDiagrama> logs = new ArrayList<>();  // LOGS ANIDADOS
}
```

---

#### LOGDIAGRAMA.java

**ANTES:** Era una entidad JPA independiente

**AHORA:** Es un sub-documento (sin @Document, sin @Id)

```java
@Data
public class LogDiagrama {
    private LocalDateTime tiempo;
    private String json;
}
```

---

### 5.4 Repositorios

**ANTES:**
```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
```

**AHORA:**
```java
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);  // Query custom
}
```

---

## 6. Cómo Usar Los Repositorios

### Ejemplo 1: Guardar un Usuario

```java
@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public Usuario crearUsuario(String username, String password) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        
        return usuarioRepository.save(usuario);  // Inserta en MongoDB
    }
}
```

---

### Ejemplo 2: Agregar un Log a una Política

```java
@Service
public class PoliticaService {
    
    @Autowired
    private PoliticaNegocioRepository politicaRepository;
    
    public void agregarLogAPolítica(String politicaId, String jsonDiagrama) {
        PoliticaNegocio política = politicaRepository.findById(politicaId)
            .orElseThrow(() -> new RuntimeException("Política no encontrada"));
        
        LogDiagrama log = new LogDiagrama();
        log.setTiempo(LocalDateTime.now());
        log.setJson(jsonDiagrama);
        
        política.getLogs().add(log);  // Agregar log a la lista
        
        politicaRepository.save(política);  // Actualizar el documento completo
    }
}
```

---

### Ejemplo 3: Buscar Usuarios por Username

```java
@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
```

---

### Ejemplo 4: Encontrar Políticas por Estado

```java
@Service
public class PoliticaService {
    
    @Autowired
    private PoliticaNegocioRepository politicaRepository;
    
    public List<PoliticaNegocio> obtenerPoliticasPorEstado(String estado) {
        return politicaRepository.findByEstado(estado);  // Ej: "borrador", "activa"
    }
}
```

---

## 7. Consideraciones Técnicas

### 7.1 ¿Qué pasa si paras MongoDB?

Si cierras MongoDB pero la aplicación intenta conectarse:
- ❌ La aplicación fallará con: `com.mongodb.MongoTimeoutException`
- **Solución:** Reinicia MongoDB antes de ejecutar Spring Boot

---

### 7.2 ¿Dónde se almacenan los datos de MongoDB?

**Con Docker:** En el contenedor (datos se pierden si removes el contenedor)
- Para persistir datos, usar volúmenes Docker:
  ```bash
  docker run -d -p 27017:27017 -v mongo_data:/data/db --name mongodb mongo:latest
  docker volume create mongo_data
  ```

**Con instalación local:** Típicamente en `C:\Program Files\MongoDB\Server\7.0\data`

---

### 7.3 Validación de Datos

Los campos marcados con `@NotBlank` se validan automáticamente:

```java
Usuario usuario = new Usuario();
usuario.setUsername("");  // ❌ Error: validation failed
usuario.setPassword(null);  // ❌ Error: validation failed

usuarioRepository.save(usuario);  // Lanza excepción
```

---

### 7.4 IDs en MongoDB

En PostgreSQL usabas `Long id` (números: 1, 2, 3, ...)

En MongoDB usas `String id` (ObjectIds: "507f1f77bcf86cd799439011")

**Automáticamente convertidos por Spring Data MongoDB** ✅

---

## 8. Troubleshooting

### ❌ Error: `connect ECONNREFUSED 127.0.0.1:27017`

**Problema:** MongoDB no está corriendo

**Solución:**
```bash
# Si usas Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Si instalaste localmente
# En Windows, MongoDB debería estar como servicio ejecutándose
# Verifica en: Servicios > MongoDB Server
```

---

### ❌ Error: `MongoTimeoutException`

**Problema:** La conexión a MongoDB está tardando demasiado o MongoDB no está disponible

**Solución:**
1. Verifica que MongoDB está corriendo: `docker ps` o del Administrador de Servicios
2. Verifica la URL en `application.properties`: `mongodb://localhost:27017`
3. Reinicia MongoDB

---

### ❌ Error: `ValidationException - Document failed validation`

**Problema:** Un campo con `@NotBlank` está vacío

**Solución:** Asegúrate que al crear Usuario o PoliticaNegocio, rellenas los campos requeridos:
```java
Usuario usuario = new Usuario();
usuario.setUsername("admin");  // ✅ Requerido
usuario.setPassword("123");    // ✅ Requerido
```

---

### ❌ Colecciones no se crean en MongoDB

**Problema:** La BD está vacía después de ejecutar la app

**Solución:**
1. Verifica que `DatabaseSeeder.java` está ejecutándose (revisa logs)
2. Verifica que la conexión MongoDB funciona:
   ```bash
   mongosh mongodb://localhost:27017/politica_db
   > db.usuarios.find()
   ```

---

## 9. Checklist: Pasos para Comenzar

- [ ] Instalar MongoDB (Docker o local)
- [ ] Verificar que MongoDB está corriendo en puerto 27017
- [ ] Ejecutar: `./mvnw.cmd spring-boot:run`
- [ ] Ver en logs: `✅ Seed: Usuario admin creado.`
- [ ] Abrir Compass o mongosh y verificar datos:
  ```bash
  mongosh
  > use politica_db
  > db.usuarios.find()
  > db.politicasNegocio.find()
  ```
- [ ] Llamar a endpoints (ejemplo: POST `/api/auth/login`)

---

## 10. Comparación: Antes vs Después

| Acción | PostgreSQL | MongoDB |
|--------|-----------|---------|
| **Instalar BD** | psql installer (complejo) | docker run (1 línea) |
| **Crear tablas** | Migraciones SQL (manual) | Automático (Spring) |
| **Insertar usuario** | `INSERT INTO usuario...` | `usuarioRepository.save(user)` |
| **Agregar log** | `INSERT INTO log_diagrama` + UPDATE política | `política.getLogs().add(log); save(política)` |
| **Buscar por username** | `SELECT * FROM usuario WHERE username=...` | `usuarioRepository.findByUsername(...)` |
| **Relaciones** | `JOIN` explícito | Embedding (nativo) |

---

## 11. FAQ

**P: ¿Necesito borrar PostgreSQL?**  
**R:** No, puedes mantenerlo. Ahora solo usas MongoDB.

**P: ¿Pierdo los datos de PostgreSQL?**  
**R:** Sí, MongoDB es BD nueva. Si necesitabas datos, exporta de PostgreSQL antes.

**P: ¿Puedo cambiar de vuelta a PostgreSQL?**  
**R:** Sí, solo cambias la config y reemplazas las anotaciones (@Entity → @Document, etc).

**P: ¿Los endpoints cambian?**  
**R:** No, `/api/auth/login`, `/api/diagrams/base/{nombre}`, etc. funcionan igual.

**P: ¿Cómo actualizo un log existente?**  
**R:** No hay "actualizar un log". Por diseño, los logs dentro de una política son inmutables. Si necesitas cambiar, agrega un nuevo log.

**P: ¿MongoDB es más rápido que PostgreSQL?**  
**R:** Depende. MongoDB es mejor para documentos complejos (JSON anidados). PostgreSQL es mejor para datos altamente relacionados. Para tu proyecto, es neutral.

---

## 12. Conclusión

✅ **La migración está lista.** Solo necesitas:

1. **Instalar MongoDB** (Docker: 1 comando)
2. **Ejecutar la app** (`./mvnw.cmd spring-boot:run`)
3. **Verificar en Compass/mongosh** que datos están creados

¡Listo para usar! 🚀
