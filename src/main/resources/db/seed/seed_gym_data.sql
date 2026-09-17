-- Seed data: university gym reservation system
-- NOT a Flyway migration (lives outside db/migration on purpose) — run manually against
-- a dev/demo DB after migrations, e.g.:
--   psql "$DATABASE_URL" -f src/main/resources/db/seed/seed_gym_data.sql
--
-- Seed users:
--   STUDENT  id=1234567890  sofia.ramirez@univgo.edu  password=Contrasena123!
--   ADMIN    id=0987654321  daniel.ortiz@univgo.edu   password=Admin123!
-- Either the id or the email works as the sign-in identifier.
-- (BCrypt hashes generated with `htpasswd -bnBC 10 "" '<password>'`)
--
-- NOTE: space_types NOT seeded here (already present in DB). All spaces
-- below use the existing space_type_id: 1798287d-6a49-4651-981c-cbefc23b51b5
--
-- Capacities are assumed (not specified): basketball 10, tennis 4 each,
-- synthetic grass (11)/(7) read as 11-a-side/7-a-side soccer -> 22/14.
-- Adjust if wrong.
--
-- All primary keys are DEFAULT gen_random_uuid() — no id is hardcoded.
-- FK links below are resolved via natural keys (identification / name)
-- inside one chained WITH statement, since the generated ids aren't
-- known until insert time.
--
-- roles/permissions/role_permissions are NOT seeded here — assumes the
-- base 'STUDENT'/'ADMIN' rows from V3__rbac.sql already exist in roles.

WITH new_users AS (
  INSERT INTO users (identification, email, first_name, last_name, password) VALUES
    ('1234567890', 'sofia.ramirez@univgo.edu',  'Sofía',  'Ramírez', '$2y$10$r4SK4wpDkunqexE22aptR.m5D87PaQNKXOTnN1Wj1ouH5q2lMC8u.'),
    ('0987654321', 'daniel.ortiz@univgo.edu',   'Daniel', 'Ortiz',   '$2y$10$194Q5TZUyHAKqXJeyKEew.xtL3tnNWfM0b.pCiVwbKZkWbYCUxxbK')
  RETURNING id, identification
),

new_user_roles AS (
  INSERT INTO user_roles (user_id, role_id)
  SELECT u.id, r.id
  FROM new_users u
  JOIN roles r ON r.name = CASE WHEN u.identification = '0987654321' THEN 'ADMIN' ELSE 'STUDENT' END
  RETURNING user_id
),

new_spaces AS (
  INSERT INTO spaces (name, capacity, space_type_id) VALUES
    ('Cancha de básquetbol',           10, '1798287d-6a49-4651-981c-cbefc23b51b5'),
    ('Cancha de Grama sintética (11)', 22, '1798287d-6a49-4651-981c-cbefc23b51b5'),
    ('Cancha de Tenis de Campo A',      4, '1798287d-6a49-4651-981c-cbefc23b51b5'),
    ('Cancha de Tenis de Campo B',      4, '1798287d-6a49-4651-981c-cbefc23b51b5'),
    ('Cancha de Grama sintética (7)',  14, '1798287d-6a49-4651-981c-cbefc23b51b5')
  RETURNING id, name
)

-- space_schedules (day_of_week: 1=Mon ... 7=Sun)
-- Outdoor courts: Mon-Fri 06:00-22:00, Sat 08:00-18:00, closed Sun.
INSERT INTO space_schedules (space_id, day_of_week, start_time, end_time)
SELECT s.id, d.dow, d.start_t, d.end_t
FROM new_spaces s
CROSS JOIN (VALUES
  (1, TIME '06:00', TIME '22:00'),
  (2, TIME '06:00', TIME '22:00'),
  (3, TIME '06:00', TIME '22:00'),
  (4, TIME '06:00', TIME '22:00'),
  (5, TIME '06:00', TIME '22:00'),
  (6, TIME '08:00', TIME '18:00')
) AS d(dow, start_t, end_t);
