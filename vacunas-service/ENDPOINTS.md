# Vacunas Service

Microservicio de catálogo e historial de vacunación de DAWI-VET. Escucha en el puerto
`8087`, se registra en Eureka como `vacunas-service` y usa exclusivamente
`vacunas_db`.

## Autenticación y roles

Todos los endpoints requieren:

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

El JWT es emitido por `auth-service`; este servicio solamente valida su firma,
expiración y los claims `id`, `sub` y `rol`. No se generan tokens locales.

| Operación | ADMIN | RECEPCIONISTA | VETERINARIO |
|---|:---:|:---:|:---:|
| Leer catálogo e historial | Sí | Sí | Sí |
| Crear, editar, activar o desactivar vacunas | Sí | No | No |
| Registrar, corregir o anular aplicaciones | Sí | No | Sí |

## Contrato de respuesta

Éxito:

```json
{
  "success": true,
  "message": "Vacuna obtenida correctamente",
  "data": {},
  "timestamp": "2026-10-14T18:30:00-05:00"
}
```

Página:

```json
{
  "success": true,
  "message": "Vacunas obtenidas correctamente",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  },
  "timestamp": "2026-10-14T18:30:00-05:00"
}
```

Error:

```json
{
  "success": false,
  "message": "Existen campos inválidos",
  "error": {
    "code": "VALIDACION_CAMPOS",
    "details": [
      {"field": "nombre", "message": "El nombre es obligatorio"}
    ]
  },
  "path": "/api/vacunas/registrar",
  "timestamp": "2026-10-14T18:30:00-05:00"
}
```

## Catálogo de vacunas

### POST `/api/vacunas/registrar`

Registra una vacuna. Rol: **ADMIN**. Respuesta: **201 Created**.

```json
{
  "nombre": "Antirrábica",
  "descripcion": "Vacuna anual",
  "enfermedadPreviene": "Rabia"
}
```

`nombre` y `enfermedadPreviene` son obligatorios. El nombre se recorta y es único
sin distinguir mayúsculas/minúsculas. `activo`, `id` y cualquier estado calculado
no forman parte del DTO de entrada y nunca se copian a la entidad.

### GET `/api/vacunas`

Lista y filtra el catálogo en base de datos. Roles: **ADMIN**, **RECEPCIONISTA**,
**VETERINARIO**. Respuesta: **200 OK**.

| Parámetro | Tipo | Regla |
|---|---|---|
| `page` | entero | Por defecto 0; mínimo 0 |
| `size` | entero | Por defecto 10; entre 1 y 100 |
| `nombre` | texto | Opcional, parcial, sin distinguir mayúsculas |
| `activo` | booleano | Opcional |
| `sortBy` | texto | `id`, `nombre`, `enfermedadPreviene`, `activo` |
| `direction` | texto | `asc` o `desc` |

Orden por defecto: `nombre asc, id asc`.

Ejemplo:

```http
GET /api/vacunas?page=0&size=10&nombre=Rabia&activo=true&sortBy=nombre&direction=asc
```

### GET `/api/vacunas/{id}`

Obtiene una vacuna activa o inactiva. Roles de lectura. Respuesta: **200** o **404**.

### PUT `/api/vacunas/{id}`

Actualiza únicamente nombre, descripción y enfermedad prevenida. Rol: **ADMIN**.
Usa el mismo body de registro. Respuesta: **200**.

### DELETE `/api/vacunas/{id}`

Desactiva lógicamente la vacuna. Rol: **ADMIN**. Respuesta: **200**.
No elimina aplicaciones históricas. Repetir la desactivación devuelve **409**.

### PATCH `/api/vacunas/{id}/activar`

Reactiva la vacuna. Rol: **ADMIN**. No requiere body. Respuesta: **200**.
Repetir la activación devuelve **409**.

## Aplicaciones e historial

### POST `/api/vacunas/aplicaciones/registrar`

Registra una aplicación después de validar vacuna, mascota y veterinario. Roles:
**ADMIN**, **VETERINARIO**. Respuesta: **201 Created**.

```json
{
  "vacunaId": 1,
  "mascotaId": 12,
  "veterinarioId": 4,
  "fechaAplicacion": "2026-10-14",
  "proximaDosis": "2027-10-14",
  "lote": "L-RAB-2026-09",
  "observacion": "Sin reacciones inmediatas"
}
```

Los tres IDs deben ser positivos. `fechaAplicacion` no puede ser futura y
`proximaDosis`, si existe, debe ser posterior. Estado, nombres copiados, datos de
anulación e ID autogenerado no son campos de entrada.

Antes de guardar se comprueba:

1. La vacuna local existe y está activa.
2. `GET /api/mascotas/{id}` devuelve el mismo ID, nombre válido y `activo=true`.
3. `GET /api/veterinarios/{id}` devuelve el mismo ID, nombre y apellido válidos y
   `activo=true`.
4. Todas las fechas y longitudes son válidas.

No se persiste una aplicación si alguna comprobación falla.

### GET `/api/vacunas/aplicaciones`

Busca el historial mediante una consulta paginada a la base de datos. Roles de
lectura. Respuesta: **200**, incluso sin coincidencias.

| Parámetro | Tipo | Regla |
|---|---|---|
| `nombreMascota` | texto | Parcial e insensible a mayúsculas |
| `mascotaId` | entero | Exacto y positivo |
| `veterinarioId` | entero | Exacto y positivo |
| `vacunaId` | entero | Exacto y positivo |
| `estado` | enum | `REGISTRADA` o `ANULADA` |
| `fechaDesde` | ISO date | Inicio inclusivo |
| `fechaHasta` | ISO date | Fin inclusivo; no anterior a `fechaDesde` |
| `page` | entero | Por defecto 0 |
| `size` | entero | Por defecto 10; máximo 100 |
| `sortBy` | texto | `id`, `fechaAplicacion`, `proximaDosis`, `mascotaNombre`, `vacunaNombre`, `veterinarioNombre`, `estado` |
| `direction` | texto | `asc` o `desc` |

Orden por defecto: `fechaAplicacion desc, id desc`.

Ejemplo:

```http
GET /api/vacunas/aplicaciones?page=0&size=10&nombreMascota=Firu&estado=REGISTRADA&sortBy=fechaAplicacion&direction=desc
```

### GET `/api/vacunas/aplicaciones/{id}`

Obtiene el detalle, también cuando está anulado. Roles de lectura. Respuesta:
**200** o **404**.

### PUT `/api/vacunas/aplicaciones/{id}`

Corrige exclusivamente fechas, lote y observación. Roles: **ADMIN**, **VETERINARIO**.
Los IDs relacionados permanecen inmutables.

```json
{
  "fechaAplicacion": "2026-10-13",
  "proximaDosis": "2027-10-13",
  "lote": "L-RAB-2026-09-CORREGIDO",
  "observacion": "Fecha verificada con la cartilla"
}
```

Respuesta: **200**. Una aplicación anulada devuelve **409**.

### PATCH `/api/vacunas/aplicaciones/{id}/anular`

Anula lógicamente y asigna la fecha/hora en el servidor. Roles: **ADMIN**,
**VETERINARIO**.

```json
{
  "motivo": "Registro duplicado en la cartilla"
}
```

El motivo es obligatorio y admite hasta 500 caracteres. Respuesta: **200**.
Una segunda anulación devuelve **409**. No existe endpoint de borrado físico.

## Códigos de error

| HTTP | Código | Uso |
|---:|---|---|
| 400 | `VALIDACION_CAMPOS` | Body con campos requeridos/longitudes inválidas |
| 400 | `JSON_INVALIDO` | JSON malformado o tipos incorrectos |
| 400 | `PARAMETROS_INVALIDOS` | Parámetro con tipo o formato inválido |
| 400 | `PAGINACION_INVALIDA` | Página o tamaño fuera de rango |
| 400 | `FILTRO_INVALIDO` | Filtro vacío o ID no positivo |
| 400 | `RANGO_FECHAS_INVALIDO` | Rango invertido |
| 400 | `ORDENAMIENTO_INVALIDO` | Campo o dirección no permitido |
| 401 | `AUTENTICACION_REQUERIDA` | Falta token Bearer |
| 401 | `TOKEN_INVALIDO` | Firma, claims o expiración inválidos |
| 403 | `ACCESO_DENEGADO` | El rol no permite la operación |
| 404 | `VACUNA_NO_ENCONTRADA` | Vacuna local inexistente |
| 404 | `APLICACION_NO_ENCONTRADA` | Aplicación inexistente |
| 404 | `MASCOTA_NO_ENCONTRADA` | Mascotas respondió 404 |
| 404 | `VETERINARIO_NO_ENCONTRADO` | Veterinarios respondió 404 |
| 409 | `VACUNA_DUPLICADA` | Nombre ya registrado |
| 409 | `VACUNA_YA_INACTIVA` / `VACUNA_YA_ACTIVA` | Transición repetida |
| 409 | `APLICACION_ANULADA` | Modificación o anulación repetida |
| 409 | `RESTRICCION_DATOS` | Restricción de persistencia controlada |
| 422 | `MASCOTA_INACTIVA` | Mascota inactiva |
| 422 | `VETERINARIO_INACTIVO` | Veterinario inactivo |
| 422 | `VACUNA_INACTIVA` | Vacuna inactiva |
| 422 | `FECHA_APLICACION_INVALIDA` | Fecha futura |
| 422 | `PROXIMA_DOSIS_INVALIDA` | Próxima dosis no posterior |
| 502 | `RESPUESTA_SERVICIO_INVALIDA` | Respuesta nula, incompleta o inconsistente |
| 502 | `FALLO_SERVICIO_EXTERNO` | Dependencia respondió error no clasificable como 404 |
| 502 | `CONTEXTO_AUTENTICACION_NO_DISPONIBLE` | No se pudo propagar Authorization |
| 503 | `SERVICIO_EXTERNO_NO_DISPONIBLE` | Sin instancia, conexión rechazada o caída |
| 504 | `SERVICIO_EXTERNO_TIMEOUT` | Timeout de conexión/lectura |
| 500 | `ERROR_INTERNO` | Error propio inesperado, sin detalles sensibles |

## Ejemplos para Postman

Use como variable de entorno:

- `baseUrl = http://localhost:8080` (Gateway), o `http://localhost:8087` directo.
- `token = <access token devuelto por auth-service>`.

### Registrar vacuna

```http
POST {{baseUrl}}/api/vacunas/registrar
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "nombre": "Antirrábica",
  "descripcion": "Refuerzo anual",
  "enfermedadPreviene": "Rabia"
}
```

### Registrar aplicación

```http
POST {{baseUrl}}/api/vacunas/aplicaciones/registrar
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "vacunaId": 1,
  "mascotaId": 12,
  "veterinarioId": 4,
  "fechaAplicacion": "2026-10-14",
  "proximaDosis": "2027-10-14",
  "lote": "L-RAB-2026-09",
  "observacion": "Aplicación sin incidentes"
}
```

### Filtrar por nombre histórico

```http
GET {{baseUrl}}/api/vacunas/aplicaciones?nombreMascota=Firu&page=0&size=10&sortBy=fechaAplicacion&direction=desc
Authorization: Bearer {{token}}
```

## Ejecución local

Requisitos: Java 17 compatible, MySQL, Eureka y los servicios externos necesarios.

En Windows:

```bat
cd vacunas-service
mvnw.cmd test
mvnw.cmd spring-boot:run
```

En Linux/macOS, con `JAVA_HOME` configurado:

```bash
cd vacunas-service
./mvnw test
./mvnw spring-boot:run
```

Hibernate mantiene `ddl-auto=update` y crea `vacunas_db` mediante
`createDatabaseIfNotExist=true`. Para registrar aplicaciones deben estar disponibles:

- Eureka en `http://localhost:8761`.
- `mascotas-service` y `veterinarios-service` registrados en Eureka.
- Un JWT válido de `auth-service`.

Los timeouts Feign son configurables:

```text
FEIGN_CONNECT_TIMEOUT=2000
FEIGN_READ_TIMEOUT=3000
```

No hay reintentos automáticos.

## Limitación deliberada de datos históricos

`mascotaNombre`, `veterinarioNombre` y `vacunaNombre` son copias históricas
capturadas al registrar la aplicación. El filtro `nombreMascota` consulta esa copia:
si el nombre cambia después en `mascotas-service`, el historial anterior **no se
actualiza automáticamente**. Esto evita llamadas Feign por cada fila y conserva el
nombre conocido en el momento de la aplicación.
