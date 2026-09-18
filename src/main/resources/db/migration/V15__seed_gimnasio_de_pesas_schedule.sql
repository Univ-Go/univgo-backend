-- Gimnasio de Pesas (9dabc028-466a-4ac0-8693-5c78b4b59929) predates V5, which only seeded
-- schedules for the five courts it created. Without a schedule the catalog generates zero blocks
-- for it — not a bug, but indistinguishable from one to a person looking at the app. Same weekly
-- pattern as every other space: Mon-Fri 06:00-22:00, Sat 08:00-18:00, closed Sun.
INSERT INTO space_schedules (space_id, day_of_week, start_time, end_time)
SELECT '9dabc028-466a-4ac0-8693-5c78b4b59929', d.dow, d.start_t, d.end_t
FROM (VALUES
  (1, TIME '06:00', TIME '22:00'),
  (2, TIME '06:00', TIME '22:00'),
  (3, TIME '06:00', TIME '22:00'),
  (4, TIME '06:00', TIME '22:00'),
  (5, TIME '06:00', TIME '22:00'),
  (6, TIME '08:00', TIME '18:00')
) AS d(dow, start_t, end_t);
