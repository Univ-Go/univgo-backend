# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & run

Java toolchain **25**, Spring Boot **4.1**, Gradle Kotlin DSL. (The README still says Java 21 / Boot 3.4 — `build.gradle.kts` is the truth.)

```bash
./gradlew dev            # custom BootRun task: runs with --spring.profiles.active=local (port 3000)
./gradlew build          # compile + test + jar (build/libs/univgo-backend.jar)
./gradlew test           # full suite — pure unit tests, no DB or network needed
./gradlew test --tests 'com.univgo.backend.reservations.domain.ReservationTimingCalculatorTest'
./gradlew test --tests '*CreateReservationServiceTest.methodName'
./gradlew compileJava
```

Swagger UI at `/swagger-ui.html`; OpenAPI JSON at `/v3/api-docs`.

## Configuration

`application.yml` reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` from the environment. Locally, copy `src/main/resources/application-local.yml.example` to `application-local.yml` (gitignored) and fill it in — credentials never go in `application.yml`.

The JDBC URL needs `?stringtype=unspecified` because several columns are native Postgres enums written as strings.

`.env.example` is a leftover from the pre-rewrite NestJS backend (`DATABASE_URL`, `PORT`) and is **not** read by Spring. Ignore it.

## Database

`spring.jpa.hibernate.ddl-auto: validate` — Hibernate never creates or alters anything. **Flyway migrations in `src/main/resources/db/migration/` are the only schema source of truth.** Existing `V1`–`V9` files are history: never edit them, add `V10__…sql`. They run automatically at startup.

`src/main/resources/db/seed/seed_gym_data.sql` lives outside `db/migration` on purpose — demo data, applied by hand with `psql`.

Ids are UUID everywhere. New reservations get an app-generated UUIDv7 (`shared/util/Uuidv7Generator`) so rows sort by creation time; other tables fall back to the DB's `gen_random_uuid()`.

## Architecture

Hexagonal (ports & adapters) per module, under `src/main/java/com/univgo/backend/`: `auth`, `users`, `spaces`, `reservations`, `shared`.

```
domain/                                  pure business objects + exceptions, zero framework imports
application/port/in/XUseCase             one interface per use case; its Command is a nested record
application/port/out/XRepositoryPort     what the domain needs from infrastructure
application/usecase/XService             @Service implementing the in-port, depends only on out-ports
infrastructure/adapter/in/web            @RestController + request/response records in dto/
infrastructure/adapter/out/persistence   JPA entity, Spring Data repo, adapter, hand-written mapper
```

Conventions that the code follows consistently:

- **Domain classes are hand-written POJOs** — no Lombok, no JPA annotations, no setters beyond intentional mutators (`Reservation.cancel`, `checkIn`). JPA entities are separate classes with a `protected` no-arg constructor.
- **Mapping is a package-private final class with static `toDomain`/`toEntity`** (`ReservationPersistenceMapper`). MapStruct and Lombok are on the classpath but effectively unused — don't introduce them into new code without a reason.
- **Constructor injection, no field injection.** `@Transactional` appears in exactly two places: the refresh-token adapter's delete queries, and `CreateReservationService`, which needs it to hold its aforo lock (below). Don't add a third without the same kind of reason.
- The `spaces` module has **no web adapter**. Space-facing HTTP endpoints (`SpacesController`, `AdminSpacesController`) live in `reservations` and call `spaces`' out-ports directly; `spaces` owns `Space`, `SpaceSchedule`, `BlockGenerator`.

## The reservation model (read this before touching `reservations`)

The whole module implements a functional spec (Spanish; quoted throughout the code comments). Its non-obvious rules:

- **State is computed, never stored.** `ReservationState` (RESERVED / IN_PROGRESS / FINISHED / EXPIRED / CANCELLED) is derived on every read by `ReservationTimingCalculator.stateAt`. `V9` dropped the `status` column; only the facts a human action produces are persisted — `created_at`, `checked_in_at`, `cancelled_at`, `cancelled_by`. Never add a status column back or cache a state.
- **All clock math lives in `ReservationTimingCalculator`**, as pure static functions taking an explicit `now`. Never inline a timing formula elsewhere. The key ones: a block stops being bookable at `end − minUsage − tolerance`; check-in opens at `max(blockStart − tolerance, createdAt)` and closes at `min(max(blockStart, createdAt) + tolerance, blockEnd − minUsage)`.
- **Capacity (aforo) is recomputed, not counted.** `OccupancyCounter` counts reservations whose *computed* state is RESERVED, IN_PROGRESS or SUSPENDED — so a plaza frees itself the moment a reservation expires. There is no occupancy counter column, and there must not be one: a counter would have to be decremented by an expiry, which nobody writes.
- **The aforo check is serialised by a Postgres advisory lock**, not by a counter. Because the figure it tests is recomputed rather than stored, nothing in the database stops two requests from counting the same last plaza as free. `CreateReservationService` takes `lockBlockForBooking` (keyed to space + date + block start) after the cheap refusals and before the count, and `@Transactional` holds it until the insert commits. This is the shared-capacity rule — right for the gym, where the aforo really is N students at once. **Spaces where one booking takes the whole room are not modelled yet**: they carry a `capacity` today and so are sold N times over. That semantic is undefined, not decided.
- **The four tunables come from the DB**, not from code: `InstitutionConfig` (block duration, check-in tolerance, minimum usage, reservations per space per day) is a singleton row (`institution_config`) editable via `PUT /admin/institution-config`. Never hardcode 120/15/75/1.
- **Students reserve a block, not a time range.** `BlockGenerator` slices a space's `space_schedules` windows into fixed-size blocks; a request whose `startTime` doesn't match a generated block start is rejected.
- **Check-in scanning always returns HTTP 200** with a `CheckInVerdict` in the body (`VALID`, `ALREADY_USED`, `EXPIRED_ALREADY`, `TOO_EARLY`, `OTHER_BLOCK`, `NOT_EXISTS`). That's a deliberate UX contract for the admin at the door — don't convert those into exceptions.

## Security

Stateless JWT. `JwtAuthenticationFilter` parses the access token, sets the user's UUID as the principal name and maps the `roles` claim to `ROLE_`-prefixed authorities. Controllers read the caller with `UUID.fromString(authentication.getName())`.

Admin surface is guarded by `@PreAuthorize("hasRole('ADMIN')")` at class level on the `/admin/**` controllers (`@EnableMethodSecurity` is on); everything else requires authentication except the `PUBLIC_ENDPOINTS` list in `SecurityConfig` (`/auth/login`, `/auth/refresh`, `/auth/logout`, swagger). Roles/permissions are an RBAC schema (`roles`, `permissions`, `user_roles`, `role_permissions`) since `V3`, not a column on `users`.

Refresh tokens are stored hashed (SHA-256, `TokenHasher`) and purged nightly by `RefreshTokenCleanupJob`.

Current routes: `/auth/{login,refresh,logout}`, `/users`, `/reservations` (`POST`, `/me`, `/{id}`, `/{id}/cancel`), `/spaces` (`GET`, `/{id}/availability`), `/admin/reservations`, `/admin/spaces/**`, `/admin/checkin/scan`, `/admin/institution-config`. The README's endpoint table predates the rewrite and is stale.

## Errors

Domain exceptions carry no HTTP knowledge; `shared/infrastructure/GlobalExceptionHandler` maps them to 404 / 401 / 400 / 409 and a `{timestamp, status, message}` body. **A new domain exception must be registered there**, or it surfaces as a 500.

## Tests

JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` ports, `@InjectMocks` service) + AssertJ. Two flavors: pure domain math tests asserting the spec's own worked examples (14:00–16:00 block, tolerance 15, min usage 75), and use-case tests with mocked ports. Everything runs without a database. Testcontainers and REST Assured are declared but no integration test uses them yet.

## Commits

Conventional Commits with a module scope: `feat(reservations): …`, `fix(seed): …`, `chore(build): …`.
