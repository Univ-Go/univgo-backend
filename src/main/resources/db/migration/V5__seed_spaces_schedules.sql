-- Seed gym courts + their weekly schedules.
-- All spaces use the existing space_type_id: 1798287d-6a49-4651-981c-cbefc23b51b5
-- Capacities assumed: basketball 10, tennis 4 each, synthetic grass (11)/(7)
-- read as 11-a-side/7-a-side soccer -> 22/14.

WITH new_spaces AS (
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
