# Inventario completo de bases de datos de DAWI-VET

Fecha de auditoría: 2026-10-16  
Alcance: código fuente y configuración presentes en el repositorio; intento de contraste con MySQL 8.4 de Docker.  
Carácter del documento: inventario estructural, sin consulta de datos de negocio y sin modificación de código o bases de datos.

## 1. Estado de la verificación y criterio de lectura

Se inspeccionaron los nueve proyectos, todos los archivos Java de los siete microservicios funcionales, sus recursos, los `pom.xml`, `compose.yaml` y el script `docker/mysql/init/01-create-databases.sh`. La búsqueda global cubrió `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, relaciones JPA, colecciones, embebidos, superclases persistentes, enumeraciones, índices y restricciones únicas. También se revisaron repositorios, servicios, controladores, DTO y clientes OpenFeign para reconstruir las referencias lógicas.

### 1.1 Limitación de la inspección física

**NO VERIFICADO:** el esquema físico de MySQL. El comando Docker de WSL no está disponible, Docker Desktop informó que no podía conectarse a `dockerDesktopLinuxEngine` y el puerto publicado `3307` no estaba escuchando. No se inició ni reinició ningún contenedor, en cumplimiento de las restricciones de la auditoría. Por ello no fue posible ejecutar `INFORMATION_SCHEMA`, `SHOW TABLES`, `SHOW COLUMNS`, `SHOW INDEX` ni `SHOW CREATE TABLE`.

Consecuencias:

- Los nombres de bases, tablas y columnas consignados como JPA son los que el código configura o declara, no una afirmación de que actualmente existan en el volumen MySQL.
- Los tipos SQL mostrados son el mapeo esperado por JPA/Hibernate para MySQL cuando puede determinarse; el tipo físico real queda **NO VERIFICADO**.
- El total de tablas/columnas físicas, claves foráneas físicas, índices físicos, `CHECK`, tablas residuales y diferencias históricas producidas por `ddl-auto=update` queda **NO VERIFICADO**.
- No se afirma que una FK, índice o restricción exista físicamente salvo que se diga expresamente; las anotaciones solo describen el DDL que Hibernate pretende mantener.

### 1.2 Convenciones usadas en las tablas

- `NV` significa **NO VERIFICADO físicamente en MySQL**.
- `SQL esperado` es el mapeo derivado del tipo Java, las anotaciones y el dialecto configurado, no el resultado de `SHOW CREATE TABLE`.
- Para `String` sin longitud explícita, JPA usa longitud 255; para los demás se muestra la longitud anotada.
- Los campos Java en camelCase se convierten a snake_case con la estrategia física predeterminada de Spring Boot; no hay estrategia personalizada configurada. Los nombres explícitos de `@Column(name=...)` no requieren esa inferencia.
- `GenerationType.IDENTITY` implica identidad/autoincremento esperado, pero `AUTO_INCREMENT` físico es NV.
- Un inicializador Java no constituye un `DEFAULT` SQL. No existe `@ColumnDefault`, `columnDefinition` ni migración que declare defaults SQL en estas entidades.

### 1.3 Cobertura JPA

Se encontraron exactamente **9 entidades JPA**. No existen `@MappedSuperclass`, `@Embeddable`, `@Embedded`, `@EmbeddedId`, `@ElementCollection`, `@Inheritance`, `@ManyToMany`, `@OneToMany` ni `@OneToOne`. La única asociación JPA entre entidades es `Usuario.rol`, anotada `@ManyToOne`. `EstadoAplicacion` es un `enum`, no una entidad ni tabla. Todos los tipos de los paquetes `dto` carecen de `@Entity` y no se presentan como tablas.

## 2. Bases de datos configuradas

| Base de datos real configurada | Microservicio | Fuente local | Fuente Docker | Diferencia con la prevista |
|---|---|---|---|---|
| `auth_db` | `auth-service` | `jdbc:mysql://localhost:3306/auth_db` | `jdbc:mysql://mysql:3306/auth_db` | Ninguna |
| `clientes_db` | `clientes-service` | `jdbc:mysql://localhost:3306/clientes_db` | `jdbc:mysql://mysql:3306/clientes_db` | Ninguna |
| `mascotas_db` | `mascotas-service` | `jdbc:mysql://localhost:3306/mascotas_db` | `jdbc:mysql://mysql:3306/mascotas_db` | Ninguna |
| `veterinarios_db` | `veterinarios-service` | `jdbc:mysql://localhost:3306/veterinarios_db` | `jdbc:mysql://mysql:3306/veterinarios_db` | Ninguna |
| `citas_db` | `citas-service` | `jdbc:mysql://localhost:3306/citas_db` | `jdbc:mysql://mysql:3306/citas_db` | Ninguna |
| `consultas_db` | `consultas-service` | `jdbc:mysql://localhost:3306/consultas_db` | `jdbc:mysql://mysql:3306/consultas_db` | Ninguna |
| `vacunas_db` | `vacunas-service` | `jdbc:mysql://localhost:3306/vacunas_db` | `jdbc:mysql://mysql:3306/vacunas_db` | Ninguna |

Los siete nombres también aparecen en el script de inicialización mediante `CREATE DATABASE IF NOT EXISTS ... CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`. El script solo crea bases y usuarios/permisos; no crea tablas. `compose.yaml` usa una única instancia `mysql:8.4`, publica `3307:3306` y monta un volumen persistente.

Todos los microservicios funcionales configuran `spring.jpa.hibernate.ddl-auto=update` en `application.properties`. El perfil Docker hereda esa propiedad del archivo base y reemplaza URL/usuario/contraseña. Esto permite que un volumen contenga objetos históricos que Hibernate no elimina; su ausencia o presencia no puede resolverse sin MySQL.

`api-gateway` y `eureka-server` **no usan persistencia relacional**: no tienen URL DataSource, dependencia JPA/JDBC/MySQL, entidades ni repositorios. No se les asigna base de datos.

---

## 3. BASE DE DATOS: `auth_db`

**Microservicio:** `auth-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 2 (`roles`, `usuarios`)  
**Columnas declaradas/esperadas por JPA:** 9

### ENTIDAD 1: `Rol`

**Clase Java:** `com.dawivet.auth.model.Rol`  
**Tabla SQL:** `roles` (`@Table` explícito)  
**Descripción:** catálogo de roles asignables a usuarios.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | No | Sí | Ninguno declarado; NV | No | `@Column(nullable=false, unique=true)` |

**Relaciones internas:** lado destino de `usuarios.rol_id`. Cardinalidad esperada `roles` 1 a N `usuarios`; `Rol` no declara colección inversa, por lo que la asociación JPA es unidireccional desde `Usuario`.

**Relaciones externas:** ninguna columna de `roles` referencia otra base. `veterinarios-service` consulta `GET /api/roles` para localizar el rol cuyo nombre sea `VETERINARIO`, pero no almacena `rol_id` en su propia base.

**Restricciones e índices:** JPA declara PK en `id` y unicidad de `nombre`. Los nombres y la existencia de los índices físicos son NV. No hay `CHECK` declarado.

**Observaciones:** no hay longitud explícita de `nombre`; la longitud JPA es 255.

### ENTIDAD 2: `Usuario`

**Clase Java:** `com.dawivet.auth.model.Usuario`  
**Tabla SQL:** `usuarios` (`@Table` explícito)  
**Descripción:** credenciales, identidad básica, rol y estado de acceso de los usuarios.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `apellido` | `apellido` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `correo` | `correo` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | No | Sí | Ninguno declarado; NV | No | `@Column(nullable=false, unique=true)` |
| `clave` | `clave` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | No | No | Ninguno declarado; NV | No | `@Column(nullable=false)`; `@JsonProperty(WRITE_ONLY)` no afecta SQL |
| `rol` | `rol_id` | `Rol` / `Integer` FK | `INT`; NV | No | Esperada por JPA hacia `roles.id`; existencia física NV | No en columna | No | Ninguno declarado; NV | No | `@ManyToOne`, `@JoinColumn(name="rol_id", nullable=false)` |
| `activo` | `activo` | `Boolean` | booleano MySQL según dialecto; NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java inicializa `true` | No | `@Column(nullable=false)` |

**Relaciones internas:** `usuarios.rol_id` N:1 hacia `roles.id`. La columna es obligatoria por `nullable=false`. `@ManyToOne` conserva su `optional=true` predeterminado a nivel de objeto, pero el `JoinColumn` exige no nulo en el DDL esperado. No se declara cascada.

**Relaciones externas:** `usuarios.id` es referenciado lógicamente por `veterinarios_db.veterinarios.usuario_id`. La relación no es una FK entre bases. Véase la matriz de relaciones externas.

**Restricciones e índices:** JPA declara PK en `id`, unicidad de `correo` y FK esperada `rol_id -> roles.id`. MySQL normalmente indexa una FK, pero el índice físico, su nombre y la propia restricción son NV. No hay `CHECK` declarado.

**Observaciones:** `activo=true` solo se aplica al construir el objeto Java; no hay default SQL. La clave se persiste como texto codificado por el servicio y se oculta en respuestas JSON, pero eso no cambia el esquema.

---

## 4. BASE DE DATOS: `clientes_db`

**Microservicio:** `clientes-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 1 (`clientes`)  
**Columnas declaradas/esperadas por JPA:** 7

### ENTIDAD 1: `Cliente`

**Clase Java:** `com.dawivet.clientes.model.Cliente`  
**Tabla SQL:** `clientes` (`@Table` explícito)  
**Descripción:** datos de contacto y estado lógico de los propietarios/clientes.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `apellido` | `apellido` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `telefono` | `telefono` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `direccion` | `direccion` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `correo` | `correo` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito; comentario Java: opcional |
| `activo` | `activo` | `Boolean` | booleano MySQL según dialecto; NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java inicializa `true` | No | `@Column(nullable=false)` |

**Relaciones internas:** ninguna.

**Relaciones externas:** `clientes.id` es destino lógico de `mascotas_db.mascotas.cliente_id` (1:N esperado por negocio). No existe asociación JPA ni FK SQL entre las bases.

**Restricciones e índices:** solo PK declarada en JPA. No se declara `UNIQUE`, FK, índice secundario ni `CHECK`. Todo índice físico adicional es NV.

**Observaciones:** `listarClientes()` devuelve todos los clientes; el endpoint `GET /api/clientes/{id}` también encuentra inactivos. Esto es relevante porque `mascotas-service` no valida `activo`. La baja es lógica (`activo=false`).

---

## 5. BASE DE DATOS: `mascotas_db`

**Microservicio:** `mascotas-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 1 (`mascotas`)  
**Columnas declaradas/esperadas por JPA:** 8

### ENTIDAD 1: `Mascota`

**Clase Java:** `com.dawivet.mascotas.model.Mascota`  
**Tabla SQL:** `mascotas` (`@Table` explícito)  
**Descripción:** datos de la mascota, estado lógico, referencia externa a su propietario y copia desnormalizada de su nombre.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `especie` | `especie` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `raza` | `raza` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `edad` | `edad` | `Integer` | `INT`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `activo` | `activo` | `Boolean` | booleano MySQL según dialecto; NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java inicializa `true` | No | `@Column(nullable=false)` |
| `clienteId` | `cliente_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(nullable=false)`; referencia lógica |
| `clienteNombre` | `cliente_nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito; copia desnormalizada |

**Relaciones internas:** ninguna.

**Relaciones externas:** `mascotas.cliente_id` N:1 lógico hacia `clientes_db.clientes.id`. En alta llama por Feign a `clientes-service`, `GET /api/clientes/{id}`. Un 404 impide el alta; por tanto verifica existencia en ese momento. Aunque el DTO contiene `activo`, el servicio no lo comprueba. La columna es obligatoria. No existe `@ManyToOne`, `@JoinColumn` ni FK entre bases.

**Restricciones e índices:** solo PK declarada. `cliente_id` no tiene índice explícito ni FK en JPA. Existencia de índices físicos adicionales: NV.

**Observaciones:** `cliente_nombre` se rellena al registrar concatenando nombre y apellido. No se refresca al cambiar los datos del cliente, por lo que es una instantánea/copia que puede divergir. La edición de mascota no cambia ni revalida `cliente_id`. La baja es lógica.

---

## 6. BASE DE DATOS: `veterinarios_db`

**Microservicio:** `veterinarios-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 1 (`veterinarios`)  
**Columnas declaradas/esperadas por JPA:** 9

### ENTIDAD 1: `Veterinario`

**Clase Java:** `com.dawivet.veterinarios.model.Veterinario`  
**Tabla SQL:** `veterinarios` (`@Table` explícito)  
**Descripción:** perfil profesional del veterinario vinculado lógicamente a un usuario de autenticación, con copia local de sus datos personales.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `usuarioId` | `usuario_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | Sí | Ninguno declarado; NV | No | `@Column(nullable=false, unique=true)`; referencia lógica |
| `nombre` | `nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |
| `apellido` | `apellido` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |
| `correo` | `correo` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |
| `especialidad` | `especialidad` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `colegiatura` | `colegiatura` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `edad` | `edad` | `Integer` | `INT`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `activo` | `activo` | `Boolean` | booleano MySQL según dialecto; NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java inicializa `true` | No | `@Column(nullable=false)` |

**Relaciones internas:** ninguna.

**Relaciones externas:** `veterinarios.usuario_id` es una relación lógica 1:1 desde el perfil hacia `auth_db.usuarios.id`: la unicidad impide dos veterinarios para el mismo `usuario_id` dentro de esta tabla. Durante el alta, el servicio llama `GET /api/roles`, elige `VETERINARIO`, luego `POST /api/auth/registrar` y guarda el `id` devuelto. No valida un usuario preexistente ni comprueba su estado activo. La obligatoriedad procede de `nullable=false`; no hay FK entre bases.

**Restricciones e índices:** PK en `id` y unicidad JPA de `usuario_id`. Nombre/existencia del índice único físico: NV. No hay FK, índice secundario o `CHECK` declarado.

**Observaciones:** nombre, apellido y correo se copian desde la solicitud usada para crear al usuario; no existe sincronización posterior. La edición solo cambia especialidad, colegiatura y edad. La baja es lógica y no desactiva automáticamente el usuario de `auth_db`.

---

## 7. BASE DE DATOS: `citas_db`

**Microservicio:** `citas-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 1 (`citas`)  
**Columnas declaradas/esperadas por JPA:** 8

### ENTIDAD 1: `Cita`

**Clase Java:** `com.dawivet.citas.model.Cita`  
**Tabla SQL:** `citas` (`@Table` explícito)  
**Descripción:** agenda de citas, con referencias externas a mascota y veterinario y copias de sus nombres.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `fecha` | `fecha` | `LocalDate` | `DATE`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo temporal implícito |
| `hora` | `hora` | `LocalTime` | `TIME` (precisión dependiente del dialecto); NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo temporal implícito |
| `estado` | `estado` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java/servicio usan `PENDIENTE` | No | `@Column(nullable=false)` |
| `mascotaId` | `mascota_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(nullable=false)`; referencia lógica |
| `mascotaNombre` | `mascota_nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |
| `veterinarioId` | `veterinario_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(nullable=false)`; referencia lógica |
| `veterinarioNombre` | `veterinario_nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |

**Relaciones internas:** ninguna.

**Relaciones externas:** `mascota_id` N:1 lógico a `mascotas_db.mascotas.id` mediante `GET /api/mascotas/{id}`; `veterinario_id` N:1 lógico a `veterinarios_db.veterinarios.id` mediante `GET /api/veterinarios/{id}`. En el alta se intercepta 404 y se exige que ambos existan. Los DTO incluyen `activo`, pero `CitaService` no valida ese valor. Ambas columnas son obligatorias y ninguna es FK SQL declarada.

**Restricciones e índices:** solo PK declarada. No hay índice explícito sobre `mascota_id`, `veterinario_id`, fecha u hora; índices físicos adicionales: NV. No hay `CHECK` para los valores de `estado`.

**Observaciones:** `mascota_nombre` y `veterinario_nombre` son instantáneas del alta. La edición solo cambia fecha y hora; no revalida referencias. El estado admite cualquier `String` desde el endpoint de cambio; cancelar asigna `CANCELADA`. No existe restricción de dominio en JPA/SQL declarada.

---

## 8. BASE DE DATOS: `consultas_db`

**Microservicio:** `consultas-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 1 (`consultas`)  
**Columnas declaradas/esperadas por JPA:** 9

### ENTIDAD 1: `Consulta`

**Clase Java:** `com.dawivet.consultas.model.Consulta`  
**Tabla SQL:** `consultas` (`@Table` explícito)  
**Descripción:** registro clínico de una consulta, con diagnóstico, tratamiento, observación y referencias externas desnormalizadas.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `fecha` | `fecha` | `LocalDate` | `DATE`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo temporal implícito |
| `diagnostico` | `diagnostico` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `tratamiento` | `tratamiento` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `observacion` | `observacion` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | mapeo básico implícito |
| `mascotaId` | `mascota_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(nullable=false)`; referencia lógica |
| `mascotaNombre` | `mascota_nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |
| `veterinarioId` | `veterinario_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(nullable=false)`; referencia lógica |
| `veterinarioNombre` | `veterinario_nombre` | `String` | `VARCHAR(255)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | copia desnormalizada |

**Relaciones internas:** ninguna.

**Relaciones externas:** `mascota_id` N:1 lógico a `mascotas_db.mascotas.id` mediante `GET /api/mascotas/{id}`; `veterinario_id` N:1 lógico a `veterinarios_db.veterinarios.id` mediante `GET /api/veterinarios/{id}`. Se valida existencia por respuesta/404 al registrar, pero no el campo `activo`, aunque está presente en ambos DTO. Las columnas son obligatorias y no son FK SQL declaradas.

**Restricciones e índices:** solo PK declarada. No hay índices secundarios, `UNIQUE`, FK o `CHECK` declarados. Estado físico: NV.

**Observaciones:** los nombres relacionados son instantáneas. La edición solo modifica fecha y datos clínicos; no cambia referencias. La eliminación usa `deleteById`, es decir, eliminación física de la fila de consulta, a diferencia de las bajas lógicas de clientes/mascotas/veterinarios.

---

## 9. BASE DE DATOS: `vacunas_db`

**Microservicio:** `vacunas-service`  
**Total de tablas físicas:** **NO VERIFICADO**  
**Tablas declaradas por JPA:** 2 (`vacunas`, `aplicaciones_vacunas`)  
**Columnas declaradas/esperadas por JPA:** 19

### ENTIDAD 1: `Vacuna`

**Clase Java:** `com.dawivet.vacunas.model.Vacuna`  
**Tabla SQL:** `vacunas` (`@Table` explícito)  
**Descripción:** catálogo de vacunas, enfermedad prevenida, descripción y estado de disponibilidad.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `nombre` | `String` | `VARCHAR(120)`; NV | No | No declarada; NV | No | Sí | Ninguno declarado; NV | No | `@Column(nullable=false, length=120)`; `@UniqueConstraint(name="uk_vacunas_nombre")` |
| `descripcion` | `descripcion` | `String` | `VARCHAR(500)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | `@Column(length=500)` |
| `enfermedadPreviene` | `enfermedad_previene` | `String` | `VARCHAR(200)`; NV | No | No declarada; NV | No | No | Ninguno declarado; NV | No | `@Column(name="enfermedad_previene", nullable=false, length=200)` |
| `activo` | `activo` | `Boolean` | booleano MySQL según dialecto; NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java/servicio usan `true` | No | `@Column(nullable=false)` |

**Relaciones internas:** `vacunas.id` es referenciado lógicamente por `aplicaciones_vacunas.vacuna_id`. No hay asociación JPA ni FK declarada; la existencia y estado activo se validan por repositorio/servicio dentro del mismo microservicio.

**Relaciones externas:** ninguna columna de esta entidad apunta a otra base.

**Restricciones e índices:** PK y restricción única JPA nombrada `uk_vacunas_nombre` sobre `nombre`. El repositorio además comprueba duplicados ignorando mayúsculas/minúsculas; esa regla de servicio no añade una segunda restricción SQL. Existencia física: NV.

**Observaciones:** el DTO exige nombre y enfermedad, aplica longitudes coincidentes y el servicio normaliza espacios. La baja es lógica.

### ENTIDAD 2: `AplicacionVacuna`

**Clase Java:** `com.dawivet.vacunas.model.AplicacionVacuna`  
**Tabla SQL:** `aplicaciones_vacunas` (`@Table` explícito)  
**Descripción:** historial de aplicaciones de vacunas, referencias a vacuna/mascota/veterinario, datos de dosis y trazabilidad de anulación.

| Campo Java | Columna SQL | Tipo Java | Tipo SQL esperado; físico | PK | FK física | NULL JPA | UNIQUE JPA | Default SQL | AI | Anotaciones JPA relevantes |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | `id` | `Integer` | `INT`; NV | Sí | No declarada; NV | No | Sí, por PK | Ninguno declarado; NV | Sí, esperado; NV | `@Id`, `@GeneratedValue(IDENTITY)` |
| `vacunaId` | `vacuna_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(name="vacuna_id", nullable=false)`; referencia interna lógica |
| `mascotaId` | `mascota_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(name="mascota_id", nullable=false)`; referencia externa lógica |
| `veterinarioId` | `veterinario_id` | `Integer` | `INT`; NV | No | **No declarada**; NV | No | No | Ninguno declarado; NV | No | `@Column(name="veterinario_id", nullable=false)`; referencia externa lógica |
| `fechaAplicacion` | `fecha_aplicacion` | `LocalDate` | `DATE`; NV | No | No declarada; NV | No | No | Ninguno declarado; NV | No | `@Column(name="fecha_aplicacion", nullable=false)` |
| `proximaDosis` | `proxima_dosis` | `LocalDate` | `DATE`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | `@Column(name="proxima_dosis")` |
| `lote` | `lote` | `String` | `VARCHAR(100)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | `@Column(length=100)` |
| `observacion` | `observacion` | `String` | `VARCHAR(500)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | `@Column(length=500)` |
| `mascotaNombre` | `mascota_nombre` | `String` | `VARCHAR(120)`; NV | No | No declarada; NV | No | No | Ninguno declarado; NV | No | `@Column(name="mascota_nombre", nullable=false, length=120)` |
| `veterinarioNombre` | `veterinario_nombre` | `String` | `VARCHAR(241)`; NV | No | No declarada; NV | No | No | Ninguno declarado; NV | No | `@Column(name="veterinario_nombre", nullable=false, length=241)` |
| `vacunaNombre` | `vacuna_nombre` | `String` | `VARCHAR(120)`; NV | No | No declarada; NV | No | No | Ninguno declarado; NV | No | `@Column(name="vacuna_nombre", nullable=false, length=120)` |
| `estado` | `estado` | `EstadoAplicacion` | representación String de longitud 20; tipo MySQL exacto NV | No | No declarada; NV | No | No | **Ninguno SQL**; Java/servicio usan `REGISTRADA` | No | `@Enumerated(STRING)`, `@Column(nullable=false, length=20)` |
| `motivoAnulacion` | `motivo_anulacion` | `String` | `VARCHAR(500)`; NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | `@Column(name="motivo_anulacion", length=500)` |
| `fechaAnulacion` | `fecha_anulacion` | `LocalDateTime` | `DATETIME` (precisión dependiente del dialecto); NV | No | No declarada; NV | Sí | No | Ninguno declarado; NV | No | `@Column(name="fecha_anulacion")` |

**Relaciones internas:** `vacuna_id` N:1 lógico hacia `vacunas.id`. `AplicacionVacunaService` usa `VacunaService.find()`/repositorio y exige que la vacuna exista y esté activa antes del alta. No hay `@ManyToOne`, `@JoinColumn` ni FK declarada, pese a estar en la misma base.

**Relaciones externas:** `mascota_id` N:1 lógico hacia `mascotas_db.mascotas.id` con `GET /api/mascotas/{id}`; `veterinario_id` N:1 lógico hacia `veterinarios_db.veterinarios.id` con `GET /api/veterinarios/{id}`. En ambos casos se valida existencia, coincidencia del ID, presencia/formato de nombres y `activo=true`. Las columnas son obligatorias; no hay FK entre bases.

**Restricciones e índices:** JPA declara los siguientes índices no únicos en `@Table`; su existencia física es NV:

| Nombre declarado | Columnas, en orden | Único |
|---|---|---|
| `idx_aplicacion_mascota_fecha` | `mascota_id`, `fecha_aplicacion` | No |
| `idx_aplicacion_veterinario` | `veterinario_id` | No |
| `idx_aplicacion_vacuna` | `vacuna_id` | No |
| `idx_aplicacion_estado_fecha` | `estado`, `fecha_aplicacion` | No |
| `idx_aplicacion_mascota_nombre` | `mascota_nombre` | No |

Además se declara la PK en `id`. No se declara FK, `UNIQUE` ni `CHECK` explícito. El tipo físico o posible `CHECK` generado automáticamente para el enum es NV y no se presume.

**Observaciones:** los tres nombres son instantáneas históricas deliberadas. El servicio limita `estado` a `REGISTRADA`/`ANULADA` mediante el enum; al anular guarda motivo y `LocalDateTime.now()`. Las reglas fecha no futura y próxima dosis posterior son validaciones de servicio, no restricciones SQL declaradas.

---

## 10. Relaciones internas y físicas

### 10.1 Asociación JPA que solicita una FK

`auth_db.roles.id (1) -> (N) auth_db.usuarios.rol_id [ASOCIACIÓN JPA; FK FÍSICA NO VERIFICADA]`

- Origen propietario JPA: `Usuario.rol`.
- Cardinalidad: muchos usuarios pueden compartir un rol; cada fila de usuario exige `rol_id` no nulo.
- Garantía declarada: `@ManyToOne` + `@JoinColumn(nullable=false)`.
- Garantía física actual: **NO VERIFICADO**. Con `ddl-auto=update`, Hibernate pretende crear/mantener una FK, pero no se obtuvo el DDL real.

### 10.2 Referencia interna por identificador, sin asociación JPA

`vacunas_db.vacunas.id (1) - - > (N) vacunas_db.aplicaciones_vacunas.vacuna_id [REFERENCIA LÓGICA INTERNA; SIN FK JPA]`

- La cardinalidad 1:N es la regla esperada por el servicio.
- `vacuna_id` es obligatorio y se valida con `VacunaService.find()` antes del alta.
- No hay `@ManyToOne`, `@JoinColumn` ni cascada.
- Una posible FK física histórica o creada manualmente es **NO VERIFICADA**; no está sustentada por el código actual.

No hay otras relaciones JPA internas, tablas puente, claves compuestas ni N:M declaradas.

## 11. Matriz exhaustiva de relaciones lógicas entre microservicios

Las ocho filas siguientes son el total de **referencias lógicas entre bases** identificado en el código. Cada fila cuenta una columna externa distinta.

| # | Servicio/entidad de origen | Campo / columna | Servicio/entidad destino | Endpoint Feign | Existencia | Estado activo | Obligatoria | Cardinalidad de negocio | Naturaleza |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `veterinarios-service` / `Veterinario` | `usuarioId` / `usuario_id` | `auth-service` / `Usuario` | `GET /api/roles` + `POST /api/auth/registrar` | El usuario se crea durante la operación; no valida uno preexistente | No | Sí (`nullable=false`) | `usuarios` 1 a 0..1 `veterinarios`, apoyada solo por `UNIQUE(usuario_id)` en origen | Lógica, sin FK entre DB |
| 2 | `mascotas-service` / `Mascota` | `clienteId` / `cliente_id` | `clientes-service` / `Cliente` | `GET /api/clientes/{id}` | Sí, al registrar; captura 404 | No, aunque el DTO trae `activo` | Sí (`nullable=false`) | `clientes` 1:N `mascotas`, regla de negocio | Lógica, sin FK entre DB |
| 3 | `citas-service` / `Cita` | `mascotaId` / `mascota_id` | `mascotas-service` / `Mascota` | `GET /api/mascotas/{id}` | Sí, al registrar; captura 404 | No, aunque el DTO trae `activo` | Sí (`nullable=false`) | `mascotas` 1:N `citas`, regla de negocio | Lógica, sin FK entre DB |
| 4 | `citas-service` / `Cita` | `veterinarioId` / `veterinario_id` | `veterinarios-service` / `Veterinario` | `GET /api/veterinarios/{id}` | Sí, al registrar; captura 404 | No, aunque el DTO trae `activo` | Sí (`nullable=false`) | `veterinarios` 1:N `citas`, regla de negocio | Lógica, sin FK entre DB |
| 5 | `consultas-service` / `Consulta` | `mascotaId` / `mascota_id` | `mascotas-service` / `Mascota` | `GET /api/mascotas/{id}` | Sí, al registrar; captura 404 | No, aunque el DTO trae `activo` | Sí (`nullable=false`) | `mascotas` 1:N `consultas`, regla de negocio | Lógica, sin FK entre DB |
| 6 | `consultas-service` / `Consulta` | `veterinarioId` / `veterinario_id` | `veterinarios-service` / `Veterinario` | `GET /api/veterinarios/{id}` | Sí, al registrar; captura 404 | No, aunque el DTO trae `activo` | Sí (`nullable=false`) | `veterinarios` 1:N `consultas`, regla de negocio | Lógica, sin FK entre DB |
| 7 | `vacunas-service` / `AplicacionVacuna` | `mascotaId` / `mascota_id` | `mascotas-service` / `Mascota` | `GET /api/mascotas/{id}` | Sí; valida respuesta e ID | **Sí** | Sí (`nullable=false`) | `mascotas` 1:N `aplicaciones_vacunas`, regla de negocio | Lógica, sin FK entre DB |
| 8 | `vacunas-service` / `AplicacionVacuna` | `veterinarioId` / `veterinario_id` | `veterinarios-service` / `Veterinario` | `GET /api/veterinarios/{id}` | Sí; valida respuesta e ID | **Sí** | Sí (`nullable=false`) | `veterinarios` 1:N `aplicaciones_vacunas`, regla de negocio | Lógica, sin FK entre DB |

Precisiones de integridad:

- Las validaciones ocurren al crear las filas. No ofrecen integridad referencial continua ante borrado, desactivación o cambio posterior en otro servicio.
- Ninguna de estas ocho referencias usa una asociación JPA; todas se almacenan como `Integer`.
- Los endpoints de destino por ID devuelven registros aunque estén inactivos en clientes, mascotas y veterinarios. Solo `vacunas-service` inspecciona expresamente `activo` para mascota y veterinario.
- `veterinarios-service` no realiza una operación compensatoria si el usuario se crea en `auth-service` y luego falla el guardado local; esto es comportamiento transaccional distribuido, no una restricción del esquema.

## 12. Relaciones listas para los diagramas

### 12.1 Relación interna declarada mediante JPA

`auth_db.roles.id (1) -> (N) auth_db.usuarios.rol_id [FK SOLICITADA POR JPA; EXISTENCIA FÍSICA NO VERIFICADA]`

### 12.2 Relación interna basada en identificador

`vacunas_db.vacunas.id (1) - - > (N) vacunas_db.aplicaciones_vacunas.vacuna_id [REFERENCIA LÓGICA INTERNA; CARDINALIDAD DE NEGOCIO]`

### 12.3 Relaciones entre bases/microservicios

`auth_db.usuarios.id (1) - - > (0..1) veterinarios_db.veterinarios.usuario_id [REFERENCIA LÓGICA; UNICIDAD SOLO EN ORIGEN VETERINARIOS]`

`clientes_db.clientes.id (1) - - > (N) mascotas_db.mascotas.cliente_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

`mascotas_db.mascotas.id (1) - - > (N) citas_db.citas.mascota_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

`veterinarios_db.veterinarios.id (1) - - > (N) citas_db.citas.veterinario_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

`mascotas_db.mascotas.id (1) - - > (N) consultas_db.consultas.mascota_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

`veterinarios_db.veterinarios.id (1) - - > (N) consultas_db.consultas.veterinario_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

`mascotas_db.mascotas.id (1) - - > (N) vacunas_db.aplicaciones_vacunas.mascota_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

`veterinarios_db.veterinarios.id (1) - - > (N) vacunas_db.aplicaciones_vacunas.veterinario_id [REFERENCIA LÓGICA; CARDINALIDAD DE NEGOCIO]`

Solo la primera relación está modelada como asociación JPA. Ninguna relación entre bases puede considerarse FK física a partir del código.

## 13. Inventario general y totales

### 13.1 Resumen por base de datos

| Base de datos | Microservicio | Tablas JPA | Tablas físicas | Cantidad de columnas JPA | Columnas físicas |
|---|---|---:|---|---:|---|
| `auth_db` | `auth-service` | 2 | **NO VERIFICADO** | 9 | **NO VERIFICADO** |
| `clientes_db` | `clientes-service` | 1 | **NO VERIFICADO** | 7 | **NO VERIFICADO** |
| `mascotas_db` | `mascotas-service` | 1 | **NO VERIFICADO** | 8 | **NO VERIFICADO** |
| `veterinarios_db` | `veterinarios-service` | 1 | **NO VERIFICADO** | 9 | **NO VERIFICADO** |
| `citas_db` | `citas-service` | 1 | **NO VERIFICADO** | 8 | **NO VERIFICADO** |
| `consultas_db` | `consultas-service` | 1 | **NO VERIFICADO** | 9 | **NO VERIFICADO** |
| `vacunas_db` | `vacunas-service` | 2 | **NO VERIFICADO** | 19 | **NO VERIFICADO** |
| **TOTAL** | **7 microservicios** | **9** | **NO VERIFICADO** | **69** | **NO VERIFICADO** |

### 13.2 Totales auditables desde el código

| Concepto | Total | Nivel de certeza |
|---|---:|---|
| Bases de datos configuradas | 7 | Verificado en URL local, URL Docker e init MySQL |
| Entidades JPA | 9 | Verificado por búsqueda e inspección de todo el código Java |
| Tablas distintas declaradas con `@Table` | 9 | Verificado en JPA |
| Atributos/columnas persistentes JPA | 69 | Verificado campo por campo |
| Claves primarias declaradas | 9 | Verificado en JPA |
| Campos `GenerationType.IDENTITY` | 9 | Verificado en JPA |
| Asociaciones JPA | 1 | `Usuario.rol` |
| FK solicitadas por asociaciones JPA | 1 | `usuarios.rol_id -> roles.id`; existencia física NV |
| Referencias lógicas internas por ID | 1 | `aplicaciones_vacunas.vacuna_id -> vacunas.id` |
| Relaciones lógicas entre microservicios | 8 | Verificado en campos, servicios y clientes Feign |
| Restricciones únicas no-PK declaradas | 4 | `roles.nombre`, `usuarios.correo`, `veterinarios.usuario_id`, `vacunas.nombre` |
| Índices secundarios explícitos declarados | 5 | Todos en `aplicaciones_vacunas` |
| DTO confundidos con entidades | 0 | Ningún DTO tiene `@Entity` |
| API Gateway/Eureka con base relacional | 0 | Sin DataSource/JPA/JDBC/MySQL |

### 13.3 Totales que requieren MySQL

| Concepto físico solicitado | Resultado |
|---|---|
| Número total de tablas físicas | **NO VERIFICADO** |
| Número total de columnas físicas | **NO VERIFICADO** |
| Número total de PK físicas | **NO VERIFICADO** |
| Número total de FK físicas | **NO VERIFICADO** |
| Tablas físicas adicionales sin entidad | **NO VERIFICADO** |
| Columnas físicas adicionales/ausentes | **NO VERIFICADO** |
| Índices y nombres físicos | **NO VERIFICADO** |
| Restricciones `CHECK` físicas | **NO VERIFICADO** |
| Defaults SQL físicos | **NO VERIFICADO** |

No se sustituyen estos valores por estimaciones. Los totales JPA de la sección anterior sí son exactos para el código auditado.

## 14. Comparación JPA frente a MySQL

### 14.1 Coincidencias comprobadas en configuración/código

- Las siete bases previstas coinciden exactamente con `application.properties`, `application-docker.properties` y el script de creación.
- Cada una de las nueve entidades declara explícitamente su tabla mediante `@Table`; no hay nombres de tabla implícitos.
- No hay scripts Flyway, Liquibase, `schema.sql`, `data.sql` ni DDL de tablas. La estructura de tablas depende de Hibernate `ddl-auto=update` y del estado previo del volumen.
- Los nombres camelCase esperados como columnas snake_case son `cliente_id`, `cliente_nombre`, `usuario_id`, `mascota_id`, `mascota_nombre`, `veterinario_id` y `veterinario_nombre`; en `AplicacionVacuna` y varios campos de `Vacuna` estos nombres están además fijados explícitamente.

### 14.2 Comparaciones que no pudieron realizarse

Por falta de acceso a MySQL se encuentran **NO VERIFICADOS**:

- entidad con tabla física correspondiente;
- tablas sin entidad actual;
- columnas residuales, faltantes o con otro nombre/tipo/longitud;
- `AUTO_INCREMENT`, nulabilidad y defaults realmente instalados;
- existencia/nombre de PK, FK, `UNIQUE`, índices y `CHECK`;
- tablas intermedias o artefactos históricos;
- restricciones declaradas en Java que Hibernate no logró crear;
- diferencias acumuladas porque `ddl-auto=update` no elimina necesariamente columnas, índices o restricciones antiguos.

### 14.3 Discrepancias o riesgos visibles solo desde el código

Estas no son discrepancias físicas confirmadas, sino diferencias entre la semántica del modelo y lo que podría suponerse erróneamente al diagramarlo:

1. `activo=true`, `Cita.estado="PENDIENTE"` y `AplicacionVacuna.estado=REGISTRADA` son defaults Java, no `DEFAULT` SQL.
2. `vacuna_id` en `aplicaciones_vacunas` relaciona dos tablas de la misma base, pero el código no declara una FK ni asociación JPA.
3. Las ocho referencias entre microservicios no son FK físicas. Su integridad depende de llamadas puntuales y reglas de aplicación.
4. Solo vacunas valida que mascota y veterinario estén activos. Mascotas, citas y consultas validan existencia pero aceptan destinos inactivos.
5. Las columnas de nombres relacionados son copias desnormalizadas; no existe sincronización automática.
6. `Cita.estado` es texto libre sin enum ni `CHECK` declarado; `EstadoAplicacion` sí es enum Java.
7. `diagnostico`, `tratamiento` y `observacion` de `Consulta` son `String` de longitud JPA 255, no `TEXT`, porque no llevan `@Lob` ni longitud distinta.
8. `correo` de `Cliente` no es único ni obligatorio en JPA; `colegiatura` de `Veterinario` tampoco es única ni obligatoria.
9. `@ManyToOne Usuario.rol` no fija `optional=false`, aunque el `JoinColumn(nullable=false)` exige una columna no nula en el DDL.

## 15. Inventario de anotaciones y elementos expresamente ausentes

| Elemento | Hallazgo |
|---|---|
| `@Entity` | 9 clases enumeradas en este documento |
| `@Table` | 9, con nombre explícito |
| `@Id` / `@GeneratedValue` | 9 / 9; todas `IDENTITY` |
| `@ManyToOne` / `@JoinColumn` | 1 / 1, en `Usuario.rol` |
| `@OneToOne` | Ninguna |
| `@OneToMany` | Ninguna |
| `@ManyToMany` / `@JoinTable` | Ninguna |
| `@Enumerated` | 1, `AplicacionVacuna.estado` con `STRING` |
| `@Embedded` / `@Embeddable` | Ninguna |
| `@ElementCollection` | Ninguna |
| `@MappedSuperclass` / herencia JPA | Ninguna |
| `@Lob` | Ninguna |
| `@Version` | Ninguna |
| Campos de auditoría `createdAt`/`updatedAt` | Ninguno |
| Tablas puente declaradas | Ninguna |
| Claves compuestas | Ninguna |

## 16. Verificación de integridad de la auditoría

- [x] Revisadas las siete bases configuradas.
- [x] Revisados API Gateway y Eureka Server; sin persistencia relacional.
- [x] Inspeccionados todos los Java y detectadas todas las clases `@Entity`.
- [x] Inspeccionadas herencia, embebidos, colecciones y relaciones; no existen elementos persistentes adicionales.
- [x] Enumerados los 69 atributos persistentes sin resumir campos.
- [x] Documentadas las nueve PK JPA y las nueve identidades.
- [x] Separada la única FK solicitada por JPA de las referencias por ID.
- [x] Documentadas las ocho referencias lógicas entre microservicios y sus endpoints/validaciones.
- [x] Documentada la referencia lógica interna vacuna-aplicación.
- [x] Documentados `UNIQUE` e índices explícitos de JPA.
- [x] Confirmados los nombres de base en las tres fuentes disponibles.
- [ ] Tablas, columnas, restricciones e índices físicos: **NO VERIFICADO**, MySQL no estaba disponible.
- [ ] Diferencias reales JPA/MySQL y objetos residuales: **NO VERIFICADO**, requieren metadatos del servidor.

## 17. Consulta de solo lectura recomendada para cerrar lo NO VERIFICADO

Cuando el contenedor ya esté en ejecución, la auditoría física puede cerrarse sin consultar datos personales ejecutando únicamente metadatos para los esquemas `auth_db`, `clientes_db`, `mascotas_db`, `veterinarios_db`, `citas_db`, `consultas_db` y `vacunas_db`:

- `INFORMATION_SCHEMA.TABLES` para tablas y conteos.
- `INFORMATION_SCHEMA.COLUMNS` para tipos completos, longitud/precisión, nulabilidad, default, `EXTRA` y orden.
- `INFORMATION_SCHEMA.TABLE_CONSTRAINTS`, `KEY_COLUMN_USAGE` y `REFERENTIAL_CONSTRAINTS` para PK, `UNIQUE` y FK.
- `INFORMATION_SCHEMA.STATISTICS` para todos los índices y orden de columnas.
- `INFORMATION_SCHEMA.CHECK_CONSTRAINTS` para `CHECK`.
- `SHOW CREATE TABLE` para validar el DDL final de cada tabla.

Hasta ejecutar esa fase, este documento representa el **100 % del modelo persistente declarado y de las relaciones sustentadas por el código**, pero no pretende representar el 100 % del contenido estructural actual de un volumen MySQL inaccesible.

