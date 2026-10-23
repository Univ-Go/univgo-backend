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

`spring.jpa.hibernate.ddl-auto: validate` — Hibernate never creates or alters anything. **Flyway migrations in `src/main/resources/db/migration/` are the only schema source of truth.** Existing files are history: never edit them, add the next version. They run automatically at startup. The verified state of the database lives in the **frontend** repo at `docs/database.md`, and a migration is not finished until that file reflects it.

Two things about spaces that are not obvious from the entities:

- **A space is never deleted, only archived** (`spaces.archived_at`, `V19`). `reservations.space_id` has no cascade, and the reservations are history. `SpaceRepositoryPort` is where the filter lives: `findAllActive`/`findActiveById`/`existsActiveById` exclude archived spaces, while plain `findById` deliberately does not — a student holding a reservation for a space that was just retired still opens its detail. Archiving also opens an indefinite closure so those reservations read as *suspended* (spec §12) rather than vanishing.
- **A space's photographs are rows** (`space_images`, `V19`), not whatever the bucket happens to contain. `position` is the order and `0` is the cover; there is no cover flag. One upload is derived into one JPEG per width in `univgo.spaces.images.widths`, stored under `spaces/{spaceId}/{imageId}/{width}.jpg`. The JDK has no WebP codec, so uploads are JPEG or PNG and derivatives are JPEG. `spaces.under_maintenance` is still orphaned and still waiting for its own migration.

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
- **Constructor injection, no field injection.** `@Transactional` is for a change that is only correct whole: the refresh-token adapter's delete queries, `CreateReservationService` holding its aforo lock (below), and the space CRUD's multi-table writes — creating a space (row + windows + photographs + stored bytes), replacing a week (delete then insert), reordering photographs (which passes through a duplicate position that only a deferred constraint tolerates) and archiving (row + closure). Don't add one without that kind of reason.
- The `spaces` module **now has a web adapter**, and only for managing the catalogue: `AdminSpaceCrudController` and `AdminSpaceTypesController` in `spaces/infrastructure/adapter/in/web`. The split is by subject, not by URL — writing a space's description, hours and photographs is not a question about reservations. The **catalogue-facing** endpoints (`SpacesController`, `AdminSpacesController`, which answer availability, blocks and closures) stay in `reservations` and keep calling `spaces`' out-ports directly. Both sets share the `/admin/spaces` base path, which Spring allows because the patterns differ.
- `spaces` must never import `reservations`. Where it needs something from there — the count of reservations archiving would suspend — the port is declared in `spaces/application/port/out` (`SpaceReservationCounterPort`) and fulfilled by `reservations` (`SpaceReservationCounter`). The consumer owns the port.
- The application layer **never imports `shared.config`**; that is an infrastructure-only import. A tunable reaches a use case through an out-port (`InstitutionConfigRepositoryPort`, `SpaceImageLimitsPort`).

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

Current routes: `/auth/{login,refresh,logout}`, `/users`, `/reservations` (`POST`, `/me`, `/{id}`, `/{id}/cancel`), `/spaces` (`GET`, `/{id}`, `/{id}/availability`), `/admin/reservations`, `/admin/spaces/**`, `/admin/space-types`, `/admin/checkin/scan`, `/admin/institution-config`. The README's endpoint table predates the rewrite and is stale.

## Errors

Domain exceptions carry no HTTP knowledge; `shared/infrastructure/GlobalExceptionHandler` maps them to 404 / 401 / 400 / 409 and a `{timestamp, status, message}` body. **A new domain exception must be registered there**, or it surfaces as a 500.

## Tests

JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` ports, `@InjectMocks` service) + AssertJ. Two flavors: pure domain math tests asserting the spec's own worked examples (14:00–16:00 block, tolerance 15, min usage 75), and use-case tests with mocked ports. Everything runs without a database. Testcontainers and REST Assured are declared but no integration test uses them yet.

## Commits

Conventional Commits with a module scope: `feat(reservations): …`, `fix(seed): …`, `chore(build): …`.
