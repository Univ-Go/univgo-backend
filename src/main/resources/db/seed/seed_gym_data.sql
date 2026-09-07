-- Seed data: university gym reservation system
-- NOT a Flyway migration (lives outside db/migration on purpose) — run manually against
-- a dev/demo DB after migrations, e.g.:
--   psql "$DATABASE_URL" -f src/main/resources/db/seed/seed_gym_data.sql
--
-- All seed users share the password: Password123!
-- (BCrypt hash below was generated with `htpasswd -bnBC 10 "" 'Password123!'`)
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
-- ADM0001 gets ADMIN, everyone else gets STUDENT via new_user_roles below.

WITH new_users AS (
  INSERT INTO users (identification, first_name, last_name, password) VALUES
    ('ADM0001',  'Laura',  'Restrepo',  '$2y$10$le8uCChsztXCqBifcF5ig.4vY1Y61esvPyhBF7/EowUoA0jUwaloG'),
    ('20231001', 'Juan',   'Perez',     '$2y$10$le8uCChsztXCqBifcF5ig.4vY1Y61esvPyhBF7/EowUoA0jUwaloG'),
    ('20231002', 'Maria',  'Gomez',     '$2y$10$le8uCChsztXCqBifcF5ig.4vY1Y61esvPyhBF7/EowUoA0jUwaloG'),
    ('20231003', 'Carlos', 'Rodriguez', '$2y$10$le8uCChsztXCqBifcF5ig.4vY1Y61esvPyhBF7/EowUoA0jUwaloG'),
    ('20231004', 'Ana',    'Martinez',  '$2y$10$le8uCChsztXCqBifcF5ig.4vY1Y61esvPyhBF7/EowUoA0jUwaloG')
  RETURNING id, identification
),

new_user_roles AS (
  INSERT INTO user_roles (user_id, role_id)
  SELECT u.id, r.id
  FROM new_users u
  JOIN roles r ON r.name = CASE WHEN u.identification = 'ADM0001' THEN 'ADMIN' ELSE 'STUDENT' END
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
),

-- space_schedules (day_of_week: 1=Mon ... 7=Sun)
-- Outdoor courts: Mon-Fri 06:00-22:00, Sat 08:00-18:00, closed Sun.
new_schedules AS (
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
  ) AS d(dow, start_t, end_t)
  RETURNING id
)

-- reservations (dates around 2026-09-07, one per status value)
INSERT INTO reservations (qr_code_data, user_id, space_id, reservation_date, start_time, end_time, status, created_at, updated_at)
SELECT v.qr, u.id, s.id, v.res_date, v.start_t, v.end_t, v.status::reservation_status_enum, v.created_at, v.updated_at
FROM (VALUES
  ('QR-D0000001', '20231001', 'Cancha de básquetbol',           '2026-09-08'::date, '18:00'::time, '19:00'::time, 'pending',            '2026-09-07 10:00:00'::timestamp, '2026-09-07 10:00:00'::timestamp),
  ('QR-D0000002', '20231002', 'Cancha de Tenis de Campo A',     '2026-09-08'::date, '07:00'::time, '08:00'::time, 'confirmed',          '2026-09-06 09:00:00'::timestamp, '2026-09-06 12:00:00'::timestamp),
  ('QR-D0000003', '20231003', 'Cancha de Grama sintética (11)', '2026-09-09'::date, '07:00'::time, '08:00'::time, 'cancelled_by_user',  '2026-09-05 08:00:00'::timestamp, '2026-09-06 07:00:00'::timestamp),
  ('QR-D0000004', '20231004', 'Cancha de Tenis de Campo B',     '2026-09-07'::date, '19:00'::time, '20:00'::time, 'cancelled_by_admin', '2026-09-04 08:00:00'::timestamp, '2026-09-06 15:00:00'::timestamp),
  ('QR-D0000005', '20231001', 'Cancha de Grama sintética (7)',  '2026-09-06'::date, '17:00'::time, '18:00'::time, 'completed',          '2026-09-03 08:00:00'::timestamp, '2026-09-06 18:00:00'::timestamp),
  ('QR-D0000006', '20231002', 'Cancha de básquetbol',           '2026-09-07'::date, '09:00'::time, '10:00'::time, 'in_progress',        '2026-09-06 10:00:00'::timestamp, '2026-09-07 09:00:00'::timestamp),
  ('QR-D0000007', '20231003', 'Cancha de Grama sintética (11)', '2026-09-05'::date, '18:00'::time, '19:00'::time, 'expired',            '2026-09-01 08:00:00'::timestamp, '2026-09-05 19:05:00'::timestamp)
) AS v(qr, user_identification, space_name, res_date, start_t, end_t, status, created_at, updated_at)
JOIN new_users  u ON u.identification = v.user_identification
JOIN new_spaces s ON s.name = v.space_name;
