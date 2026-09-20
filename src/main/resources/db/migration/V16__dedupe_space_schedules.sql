-- The gym's weekly schedule was inserted by hand in the Neon console while the feature was being
-- built, and V15 seeded the same six rows again. This is not cosmetic: BlockGenerator walks the
-- rows of a day one by one, so a duplicated window generates every block of that day twice and the
-- catalog offers the gym's slots in pairs.
--
-- ctid is the physical row address, which is the only thing telling two identical rows apart: the
-- table has no natural key and the surrogate id says nothing about which insert came first.
DELETE FROM space_schedules a
USING space_schedules b
WHERE a.ctid > b.ctid
  AND a.space_id = b.space_id
  AND a.day_of_week = b.day_of_week
  AND a.start_time = b.start_time
  AND a.end_time = b.end_time;

-- What keeps the next hand-written insert from doing it again. A space cannot open twice on the
-- same day at the same hour: that is one window, not two.
ALTER TABLE space_schedules
  ADD CONSTRAINT space_schedules_window_unique UNIQUE (space_id, day_of_week, start_time, end_time);
