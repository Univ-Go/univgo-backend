<p align="center">
  <img width="1867" height="564" alt="image" src="https://github.com/user-attachments/assets/8c0c3457-e246-4f02-994b-0182deda2473" />
</p>

# UnivGo — API

Backend de **UnivGo**, la plataforma para reservar espacios deportivos y el
gimnasio de la universidad. Este repositorio contiene únicamente la API; el
cliente web vive fuera de él.

Construido con **Java 21**, **Spring Boot 3.4** (Web, Data JPA, Security),
**PostgreSQL** (Neon) y **Flyway** para migraciones. Arquitectura hexagonal
(ports & adapters) por módulo. Anteriormente el backend era NestJS/TypeORM —
esa versión fue reemplazada por completo.

## Requisitos

- JDK 21
- Una base de datos PostgreSQL

## Puesta en marcha

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# completá datasource.url/username/password y jwt.secret

./gradlew bootRun --args='--spring.profiles.active=local'   # http://localhost:3000
```

Las credenciales van en `application-local.yml` (gitignored), nunca en
`application.yml`. Las migraciones de Flyway (`src/main/resources/db/migration/`)
también están gitignored a propósito — se aplican solas al levantar la app.

### Configuración

`application.yml` lee todo por variable de entorno o por el perfil `local`:

| Propiedad                | Descripción                                   |
| ------------------------ | ---------------------------------------------- |
| `spring.datasource.url`  | JDBC URL de PostgreSQL (`?stringtype=unspecified` requerido por los enums nativos). |
| `spring.datasource.username/password` | Credenciales de la base de datos. |
| `jwt.secret`             | Secreto para firmar los JWT.                   |
| `jwt.expiration`         | Vigencia del token en milisegundos.            |
| `server.port`            | Puerto HTTP. Por defecto `3000`.               |
| `aws.s3.endpoint`        | Endpoint S3-compatible del bucket (Neon).      |
| `aws.s3.region`          | Región que reporta el bucket.                  |
| `aws.s3.access-key-id` / `aws.s3.secret-access-key` | Credenciales del bucket. Sin acceso de escritura desde la app: las imágenes se suben a mano. |
| `aws.s3.bucket`          | Nombre del bucket. Fotos de espacio en `spaces/{spaceId}/{archivo}`. |

## Comandos

| Comando                                                  | Qué hace                          |
| --------------------------------------------------------- | ---------------------------------- |
| `./gradlew bootRun --args='--spring.profiles.active=local'` | Levanta el servidor.             |
| `./gradlew build`                                          | Compila y empaqueta el jar.       |
| `./gradlew test`                                           | Corre los tests.                  |
| `./gradlew compileJava`                                    | Solo compila.                     |

## Endpoints

| Método   | Ruta                     | Descripción                          | Auth       |
| -------- | ------------------------ | ------------------------------------ | ---------- |
| `POST`   | `/auth/login`             | Inicio de sesión, devuelve el JWT.   | pública    |
| `GET`    | `/users`                  | Lista de usuarios.                   | requerida  |
| `GET`    | `/users/:id`               | Detalle de un usuario.               | requerida  |
| `PATCH`  | `/users/:id`               | Actualiza un usuario (parcial).      | requerida  |
| `DELETE` | `/users/:id`               | Elimina un usuario.                  | requerida  |
| `POST`   | `/reservations`            | Crea una reserva.                    | requerida  |
| `GET`    | `/reservations`            | Lista todas las reservas.            | ADMIN      |
| `GET`    | `/reservations/me`         | Reservas del usuario autenticado.    | requerida  |
| `GET`    | `/reservations/:id`         | Detalle de una reserva.              | requerida  |
| `PATCH`  | `/reservations/:id/status`  | Cambia el estado de una reserva.     | ADMIN      |
| `DELETE` | `/reservations/:id`         | Elimina una reserva.                 | ADMIN      |

Autenticación por JWT (`Authorization: Bearer <token>`), obtenido en
`/auth/login`. Documentación interactiva en `/swagger-ui.html`.

## Estructura

Arquitectura hexagonal por módulo, bajo `src/main/java/com/univgo/backend/`:

```
auth/           login y emisión/validación de JWT
users/          usuarios
spaces/         soporte de solo lectura para validar espacios (aún sin CRUD propio)
reservations/   reservas e invitados
shared/         configuración de seguridad, manejo global de errores, utilidades
```

Cada módulo se divide en:

```
domain/                 entidades y reglas de negocio, sin dependencias externas
application/port/in     casos de uso (interfaces)
application/port/out    puertos hacia infraestructura (interfaces)
application/usecase      implementación de los casos de uso
infrastructure/adapter/in/web          controllers y DTOs
infrastructure/adapter/out/persistence  entidades JPA, repos y mappers
```

## Roadmap

- Gestión de espacios por parte del administrador (crear, editar espacios y horarios).
- Generación, verificación y expiración de QR para las reservas.
- Reserva de salones y espacios académicos.
- Consulta de notas y certificados.
- Evaluación de docentes.
- Cancelación de materias.
- Soporte para múltiples universidades (modelo SaaS).

## Equipo

- Juan Pablo Cardona Bedoya
- Samuel Alvarez Henao
- Tomas Cordoba Urquijo

## Licencia

MIT — ver [LICENSE](./LICENSE).
