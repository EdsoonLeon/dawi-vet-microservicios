# DAWI-VET Microservicios

Migre mi sistema de veterinaria (antes un monolito con Spring Boot) a una arquitectura de microservicios con Java 17, Spring Boot 4.1.1 y Spring Cloud 2025.1.3.

Todas las peticiones entran por el **API Gateway (puerto 8080)**, que consulta a **Eureka** dónde está cada servicio. La seguridad usa **JWT**: `auth-service` genera el token y cada servicio lo valida con la misma clave.

## Servicios

| Servicio | Puerto | Base de datos | Función |
|---|---|---|---|
| eureka-server | 8761 | - | Registro de servicios |
| api-gateway | 8080 | - | Puerta de entrada y CORS |
| auth-service | 8081 | auth_db | Usuarios, roles, login y JWT |
| clientes-service | 8082 | clientes_db | Clientes |
| veterinarios-service | 8083 | veterinarios_db | Veterinarios (llama a auth-service) |
| citas-service | 8084 | citas_db | Citas (llama a mascotas y veterinarios) |
| consultas-service | 8085 | consultas_db | Consultas (llama a mascotas y veterinarios) |
| mascotas-service | 8086 | mascotas_db | Mascotas (llama a clientes) |
| vacunas-service | 8087 | vacunas_db | Catálogo e historial de vacunación |

Próximamente: `inventario-service` (8088).

## Requisitos

- JDK 17
- MySQL 8.0 en Windows mediante el servicio `MySQL80`, disponible en `localhost:3306`
- Git y Postman
- Spring Tools (opcional, también se ejecuta por terminal)

## 1. Clonar el proyecto

```bash
git clone https://github.com/EdsoonLeon/dawi-vet-microservicios.git
cd dawi-vet-microservicios
```

## 2. Ejecutar el backend

Los servicios se levantan **uno por uno, en este orden**. Espera a que cada uno termine de arrancar (mensaje `Started ... in x seconds`) antes de iniciar el siguiente. En Spring Tools: clic derecho en el proyecto → **Run As → Spring Boot App**. La primera vez Maven descarga las dependencias y tarda unos minutos.

Las URL JDBC usan `DB_HOST`, cuyo valor predeterminado es `localhost`. Por eso los
servicios pueden ejecutarse directamente en Windows sin activar perfiles.

```
MySQL encendido (puerto 3306)
        │
        ▼
1. eureka-server (8761)
        │   abrir http://localhost:8761 y ver que carga
        ▼
2. api-gateway (8080)
        │
        ▼
3. auth-service (8081)
        │
        ▼
4. clientes-service (8082)
        │
        ▼
5. veterinarios-service (8083)
        │   necesita auth-service encendido
        ▼
6. mascotas-service (8086)
        │   necesita clientes-service encendido
        ▼
7. citas-service (8084)
        │   necesita mascotas y veterinarios encendidos
        ▼
8. consultas-service (8085)
        │   necesita mascotas y veterinarios encendidos
        ▼
9. vacunas-service (8087)
        │   necesita mascotas y veterinarios encendidos
        ▼
Abrir http://localhost:8761 y verificar que los 8 clientes Eureka
aparezcan registrados (tarda unos 30 segundos)
```

Las siete bases deben existir en MySQL80. Hibernate crea o actualiza sus tablas al
arrancar cada servicio mediante `ddl-auto=update`.

## 3. Datos iniciales

Al arrancar, `auth-service` comprueba por nombre los roles `ADMIN`, `VETERINARIO`
y `RECEPCIONISTA`, y crea únicamente los que falten. Los IDs son generados por
JPA/MySQL. El proceso es idempotente y no modifica roles existentes, por lo que ya
no se requieren `INSERT` manuales para inicializar este catálogo.

### Administrador automático para desarrollo

`auth-service` crea automáticamente la cuenta ADMIN académica al arrancar. El correo
y la contraseña predeterminados están en `application.properties` y pueden
sobrescribirse mediante `DAWIVET_ADMIN_EMAIL` y `DAWIVET_ADMIN_PASSWORD`. No se
requiere activar ningún perfil.

Hibernate mantiene `spring.jpa.hibernate.ddl-auto=update`: crea las tablas antes de
ejecutar el inicializador si todavía no existen. Después, el inicializador crea el
rol `ADMIN` si falta y busca el correo normalizado. Una cuenta existente nunca se
sobrescribe; si pertenece a otro rol, se informa el conflicto en el log sin cambiar
sus permisos. La contraseña se codifica con BCrypt y nunca se escribe en logs.

La contraseña corta de pruebas es una excepción exclusiva de este inicializador.
El registro y la edición HTTP siguen exigiendo un mínimo de 12 caracteres.

### Aprovisionamiento administrativo seguro

El registro HTTP requiere un JWT de ADMIN y no sirve para crear la primera cuenta.
El bootstrap es un comando de una sola ejecución: no abre endpoints, está
deshabilitado por defecto, localiza el rol por el nombre `ADMIN` y no modifica una
cuenta administradora existente.

Requisitos previos:

- `.env` configurado según la sección **Ejecución con Docker**;
- MySQL y `auth-service` iniciados al menos una vez para crear las tablas;
- imagen actualizada: `docker compose build auth-service`.

Desde PowerShell, en la raíz del repositorio, ejecuta lo siguiente. La contraseña
debe tener al menos 12 caracteres, se solicita sin mostrarla y sólo se guarda en un
archivo temporal fuera del repositorio:

```powershell
$adminNombre = Read-Host "Nombre del administrador"
$adminApellido = Read-Host "Apellido del administrador"
$adminCorreo = Read-Host "Correo del administrador"
$adminClaveSegura = Read-Host "Contraseña inicial (mínimo 12 caracteres)" -AsSecureString
$bootstrapDir = Join-Path ([IO.Path]::GetTempPath()) ("dawi-vet-bootstrap-" + [Guid]::NewGuid())
$passwordFile = Join-Path $bootstrapDir "admin-password"
$passwordPointer = [IntPtr]::Zero

try {
    New-Item -ItemType Directory -Path $bootstrapDir | Out-Null
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($adminClaveSegura)
    $passwordText = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    [IO.File]::WriteAllText($passwordFile, $passwordText, [Text.UTF8Encoding]::new($false))
    $passwordText = $null

    $env:DAWIVET_BOOTSTRAP_ADMIN_NOMBRE = $adminNombre
    $env:DAWIVET_BOOTSTRAP_ADMIN_APELLIDO = $adminApellido
    $env:DAWIVET_BOOTSTRAP_ADMIN_CORREO = $adminCorreo

    docker compose run --rm --no-deps `
      -e DAWIVET_BOOTSTRAP_ADMIN_NOMBRE `
      -e DAWIVET_BOOTSTRAP_ADMIN_APELLIDO `
      -e DAWIVET_BOOTSTRAP_ADMIN_CORREO `
      --volume "${bootstrapDir}:/run/secrets/dawi-vet-bootstrap:ro" `
      auth-service `
      --spring.main.web-application-type=none `
      --eureka.client.enabled=false `
      --spring.cloud.discovery.enabled=false `
      --dawivet.bootstrap.admin.enabled=true `
      --dawivet.bootstrap.admin.password-file=/run/secrets/dawi-vet-bootstrap/admin-password

    if ($LASTEXITCODE -ne 0) { throw "El bootstrap ADMIN falló" }
}
finally {
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    Remove-Item -Recurse -Force $bootstrapDir -ErrorAction SilentlyContinue
    Remove-Item Env:\DAWIVET_BOOTSTRAP_ADMIN_NOMBRE -ErrorAction SilentlyContinue
    Remove-Item Env:\DAWIVET_BOOTSTRAP_ADMIN_APELLIDO -ErrorAction SilentlyContinue
    Remove-Item Env:\DAWIVET_BOOTSTRAP_ADMIN_CORREO -ErrorAction SilentlyContinue
    $adminClaveSegura = $null
}
```

El mensaje `Bootstrap ADMIN completado correctamente` confirma la creación. Si ya
existe cualquier usuario con rol `ADMIN`, el comando termina con el mensaje
`Bootstrap ADMIN omitido` y no crea ni modifica cuentas. La opción que habilita el
bootstrap sólo se aplica al contenedor temporal; no debe añadirse a `.env` ni a
`compose.yaml`.

Iniciar sesión con `POST http://localhost:8080/api/auth/login`:

```json
{ "correo": "<correo indicado en el bootstrap>", "clave": "<contraseña indicada en el bootstrap>" }
```

Una respuesta `200` con un `token` confirma que la cuenta quedó operativa. En
Postman, para las demás peticiones usa **Authorization → Bearer Token**. El token
dura 24 horas. No copies la contraseña al README, `.env` ni scripts versionados.

El administrador automático se prueba del mismo modo. En Angular, inicia el frontend
con `npm start`, abre `http://localhost:4200/login` e introduce sus credenciales. El
frontend apunta al Gateway en `http://localhost:8080/api` y redirige el rol ADMIN a
su panel.

## 4. Guía de las APIs

Todas se prueban por el gateway (`http://localhost:8080`) y exigen el Bearer Token,
excepto `POST /api/auth/login` y `GET /api/roles`. El registro y la administración
de usuarios requieren rol `ADMIN`. Las contraseñas nuevas deben tener al menos 12
caracteres y no superar los 72 bytes admitidos por BCrypt.

### Auth (`/api/auth`, `/api/usuarios`, `/api/roles`)

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/auth/registrar` | `{ "nombre", "apellido", "correo", "clave", "rol": { "id": "<id del rol>" } }` |
| POST | `/api/auth/login` | `{ "correo", "clave" }` |
| GET | `/api/roles` | - |
| GET | `/api/usuarios` | - |
| GET | `/api/usuarios/{id}` | - |
| PUT | `/api/usuarios/{id}` | `{ "nombre", "apellido", "correo", "rol": { "id": 1 } }` |
| PATCH | `/api/usuarios/{id}/activar` | - |
| DELETE | `/api/usuarios/{id}` | - |

### Clientes (`/api/clientes`)

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/clientes/registrar` | `{ "nombre": "Juan", "apellido": "Perez", "telefono": "999888777", "direccion": "Av. Lima 123", "correo": "juan@mail.com" }` |
| GET | `/api/clientes` | - |
| GET | `/api/clientes/{id}` | - |
| PUT | `/api/clientes/{id}` | mismos campos del registro |
| PATCH | `/api/clientes/{id}/activar` | - |
| DELETE | `/api/clientes/{id}` | - (eliminación lógica) |

### Veterinarios (`/api/veterinarios`)

Requiere `auth-service` encendido y un JWT de ADMIN, porque al registrar crea el
usuario con rol VETERINARIO.

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/veterinarios/registrar` | `{ "especialidad": "Cirugia", "colegiatura": "CMVP-1234", "edad": 35, "usuario": { "nombre": "Ana", "apellido": "Lopez", "correo": "ana@vet.com", "clave": "ClaveSegura123" } }` |
| GET | `/api/veterinarios` | - |
| GET | `/api/veterinarios/{id}` | - |
| PUT | `/api/veterinarios/{id}` | `{ "especialidad", "colegiatura", "edad" }` |
| PATCH | `/api/veterinarios/{id}/activar` | - |
| DELETE | `/api/veterinarios/{id}` | - |

### Mascotas (`/api/mascotas`)

Requiere `clientes-service` encendido, porque valida que el cliente exista.

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/mascotas/registrar` | `{ "nombre": "Firulais", "especie": "Perro", "raza": "Labrador", "edad": 3, "clienteId": 1 }` |
| GET | `/api/mascotas` | - |
| GET | `/api/mascotas/cliente/{clienteId}` | - |
| GET | `/api/mascotas/{id}` | - |
| PUT | `/api/mascotas/{id}` | `{ "nombre", "especie", "raza", "edad" }` |
| PATCH | `/api/mascotas/{id}/activar` | - |
| DELETE | `/api/mascotas/{id}` | - |

### Citas (`/api/citas`)

Requiere `mascotas-service` y `veterinarios-service` encendidos.

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/citas/registrar` | `{ "fecha": "2026-10-20", "hora": "10:30:00", "mascotaId": 1, "veterinarioId": 1 }` |
| GET | `/api/citas` | - |
| GET | `/api/citas/mascota/{mascotaId}` | - |
| GET | `/api/citas/veterinario/{veterinarioId}` | - |
| GET | `/api/citas/{id}` | - |
| PUT | `/api/citas/{id}` | `{ "fecha", "hora" }` |
| PATCH | `/api/citas/{id}/estado` | `{ "estado": "ATENDIDA" }` |
| DELETE | `/api/citas/{id}` | - (deja la cita en CANCELADA) |

### Consultas (`/api/consultas`)

Requiere `mascotas-service` y `veterinarios-service` encendidos.

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/consultas/registrar` | `{ "fecha": "2026-10-20", "diagnostico": "Gastritis", "tratamiento": "Dieta blanda", "observacion": "Control en 7 dias", "mascotaId": 1, "veterinarioId": 1 }` |
| GET | `/api/consultas` | - |
| GET | `/api/consultas/mascota/{mascotaId}` | - |
| GET | `/api/consultas/veterinario/{veterinarioId}` | - |
| GET | `/api/consultas/{id}` | - |
| PUT | `/api/consultas/{id}` | `{ "fecha", "diagnostico", "tratamiento", "observacion" }` |
| DELETE | `/api/consultas/{id}` | - (eliminación física) |

### Orden sugerido para probar todo

1. Login (token)
2. Registrar cliente → registrar mascota (con el `clienteId`)
3. Registrar veterinario
4. Registrar cita y consulta (con `mascotaId` y `veterinarioId`)

## Problemas comunes

| Síntoma | Causa y solución |
|---|---|
| `403` | Falta el Bearer Token en Postman |
| `401` "Token jwt inválido o expirado" | El token venció, o la `jwt.secret` no es igual en todos los servicios |
| `503` desde el gateway | El servicio aún no se registró en Eureka (esperar) o no está encendido |
| Un servicio arranca en el puerto 8080 o falla con `jwt.secret` | No está leyendo su `application.properties`: revisar que no tenga tildes ni ñ (ni en comentarios) y limpiar `target` |
| `git push` rechazado | Hacer `git pull --rebase` y volver a `git push` |

## Trabajo con Git

```bash
git pull --rebase                 # antes de empezar cada día
git status                        # no debe aparecer target/
git add .
git commit -m "mensaje"
git push
```

## Ejecución con Docker

Cada servicio utiliza un único `application.properties`. En Windows se aplican los
valores predeterminados `localhost`; Compose suministra `DB_HOST` con
`host.docker.internal` y `EUREKA_URL` con la dirección interna de Eureka. No se
utilizan perfiles Spring.

### Requisitos previos

- Docker Desktop con contenedores Linux.
- Docker Compose v2 (incluido en Docker Desktop).
- MySQL80 instalado y ejecutándose en Windows en el puerto `3306`.
- Al menos 4 GB de memoria disponibles para Docker.
- Puertos libres en Windows para Docker: `8080` y `8761`.

En Docker Desktop, comprueba que el motor esté iniciado. Si trabajas desde WSL,
habilita **Settings → Resources → WSL Integration** para la distribución utilizada.
También puedes ejecutar los comandos directamente desde PowerShell.

### Configurar secretos locales

Desde la raíz del repositorio:

```powershell
Copy-Item .env.example .env
notepad .env
```

Reemplaza todos los valores `CHANGE_ME`. `.env` está ignorado por Git y no debe
subirse. Las contraseñas MySQL admiten letras, números, punto, guion y guion bajo.
Cada contraseña debe coincidir con la del usuario `dawivet_*` correspondiente en
MySQL80.
`JWT_SECRET` debe tener al menos 32 caracteres y es compartido por todos los
servicios.

Para sobrescribir opcionalmente las credenciales del administrador académico, agrega
a tu `.env` local:

```dotenv
DAWIVET_ADMIN_EMAIL=<correo de desarrollo>
DAWIVET_ADMIN_PASSWORD=<contraseña de desarrollo>
```

Validar la configuración antes de construir:

```powershell
docker compose config --quiet
```

### Construir e iniciar

```powershell
docker compose up -d --build
docker compose ps
```

Compose inicia nueve contenedores Java: Eureka, los siete servicios de negocio y el
Gateway. MySQL80 permanece como servicio independiente de Windows. Los Dockerfile
compilan cada JAR dentro de una imagen Maven Java 17; no usan los JAR generados por
Eclipse.

Comprobar logs:

```powershell
docker compose logs -f
docker compose logs -f vacunas-service
```

Accesos desde Windows:

- Eureka: http://localhost:8761
- API Gateway: http://localhost:8080
- MySQL Workbench: host `127.0.0.1`, puerto `3306`.

Los puertos 8081–8087 no se publican. Las peticiones Postman deben entrar por el
Gateway. MySQL usa usuarios separados: `dawivet_auth`, `dawivet_clientes`,
`dawivet_veterinarios`, `dawivet_mascotas`, `dawivet_citas`,
`dawivet_consultas` y `dawivet_vacunas`; cada uno sólo tiene permisos sobre su
base. Usa en Workbench la contraseña correspondiente de `.env`.

En MySQL80 deben existir `auth_db`, `clientes_db`, `veterinarios_db`, `mascotas_db`,
`citas_db`, `consultas_db` y `vacunas_db`, junto con sus usuarios limitados a cada
base. Las bases conviven con las de otros proyectos del servidor Windows. Hibernate
conserva `ddl-auto=update` y crea/actualiza las tablas al arrancar cada servicio.

Los tres roles estándar de `auth_db` se inicializan automáticamente. En desarrollo,
la cuenta ADMIN de pruebas también puede crearse con el procedimiento de la sección
**Datos iniciales**.

### Operación habitual

Apagar los contenedores Java:

```powershell
docker compose down
```

Este comando no detiene MySQL80 ni modifica sus bases de datos.

Reiniciar un servicio:

```powershell
docker compose restart vacunas-service
```

Reconstruirlo después de cambiar código:

```powershell
docker compose up -d --build vacunas-service
```

Ver el estado y la salud:

```powershell
docker compose ps
docker compose logs --tail=100 api-gateway
```

### Spring Tools frente a Docker

| Spring Tools/local | Docker Compose |
|---|---|
| Sin perfil Spring | Sin perfil Spring |
| MySQL en `localhost:3306` | MySQL80 en `host.docker.internal:3306` |
| Eureka en `localhost:8761` | Eureka interno en `eureka-server:8761` |
| Servicios iniciados manualmente | Compose inicia nueve contenedores Java |
| Clave JWT de configuración local | `JWT_SECRET` de `.env` |

No ejecutes simultáneamente los servicios locales en los puertos 8080/8761 y sus
equivalentes Docker publicados. Compose no finaliza procesos ajenos si un puerto está
ocupado.

