<p align="center">
  <img src="https://drive.google.com/uc?export=download&id=1G5DRy_K0lIGtc7k4Qthxg0D5LtSwoLsR" alt="UnivGoLogo" width="300"/>
</p>

# UnivGo — API

Backend de **UnivGo**, la plataforma para reservar espacios deportivos y el
gimnasio de la universidad. Este repositorio contiene únicamente la API; el
cliente web vive fuera de él.

Construido con [NestJS](https://nestjs.com/) 11, TypeORM y PostgreSQL.

## Requisitos

- Node.js 20+
- Una base de datos PostgreSQL

## Puesta en marcha

```bash
npm install
cp .env.example .env   # y completa los valores
npm run start:dev      # http://localhost:10000/api
```

### Variables de entorno

| Variable         | Descripción                                              |
| ---------------- | -------------------------------------------------------- |
| `DATABASE_URL`   | Cadena de conexión de PostgreSQL (SSL activado).          |
| `JWT_SECRET`     | Secreto para firmar los tokens de acceso.                 |
| `JWT_EXPIRES_IN` | Vigencia del token, p. ej. `1d`.                          |
| `PORT`           | Puerto HTTP. Por defecto `10000`.                         |

## Scripts

| Script               | Qué hace                                  |
| -------------------- | ----------------------------------------- |
| `npm run start:dev`  | Servidor en modo watch.                   |
| `npm run build`      | Compila a `dist/`.                        |
| `npm run start:prod` | Ejecuta el build (`node dist/main`).      |
| `npm test`           | Tests unitarios (Jest).                   |
| `npm run test:e2e`   | Tests end-to-end.                         |
| `npm run lint`       | ESLint con `--fix`.                       |
| `npm run format`     | Prettier sobre `src/` y `test/`.          |

## Endpoints

Todas las rutas cuelgan del prefijo global `/api`. Un `AuthGuard` global protege
la API; las rutas públicas se marcan con el decorador `@Public()`.

| Método   | Ruta                                | Descripción                          |
| -------- | ----------------------------------- | ------------------------------------ |
| `GET`    | `/api/ping`                         | Health check.                        |
| `POST`   | `/api/auth/sign-in`                 | Inicio de sesión, devuelve el JWT.    |
| `GET`    | `/api/auth/profile`                 | Datos del usuario autenticado.        |
| `GET`    | `/api/users`                        | Lista de usuarios.                    |
| `GET`    | `/api/users/:id`                    | Detalle de un usuario.                |
| `PATCH`  | `/api/users/:id`                    | Actualiza un usuario.                 |
| `DELETE` | `/api/users/:id`                    | Elimina un usuario.                   |
| `GET`    | `/api/spaces/name/:name`            | Busca un espacio por nombre.          |
| `POST`   | `/api/reservations`                 | Crea una reserva.                     |
| `GET`    | `/api/reservations`                 | Lista de reservas.                    |
| `GET`    | `/api/reservations/:id`             | Detalle de una reserva.               |
| `GET`    | `/api/reservations/user/:identification` | Reservas de un usuario.          |
| `PATCH`  | `/api/reservations/:id`             | Actualiza una reserva.                |
| `DELETE` | `/api/reservations/:id`             | Cancela una reserva.                  |

## Estructura

```
src/
├── auth/           autenticación JWT, guard global y decorador @Public
├── users/          usuarios
├── spaces/         espacios, tipos de espacio y horarios
├── reservations/   reservas e invitados
└── ping/           health check
```

Cada módulo sigue la convención de Nest: `*.module.ts`, `*.controller.ts`,
`*.service.ts`, más `entities/` y `dto/` cuando aplica.

## Roadmap

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
