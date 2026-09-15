-- Single-row table for the four institution-wide parameters the functional
-- spec calls "configuración de la institución, no reglas escritas en el
-- código": block duration, check-in tolerance, minimum guaranteed usage, and
-- reservations allowed per space per day. Aforo stays on spaces.capacity —
-- the spec makes capacity explicitly "por espacio", not institution-wide.
CREATE TABLE institution_config (
  id SMALLINT PRIMARY KEY DEFAULT 1,
  block_duration_minutes INTEGER NOT NULL CHECK (block_duration_minutes > 0),
  check_in_tolerance_minutes INTEGER NOT NULL CHECK (check_in_tolerance_minutes >= 0),
  min_usage_minutes INTEGER NOT NULL CHECK (min_usage_minutes >= 0),
  reservations_per_space_per_day INTEGER NOT NULL CHECK (reservations_per_space_per_day > 0),
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_institution_config_singleton CHECK (id = 1)
);

INSERT INTO institution_config (
  id, block_duration_minutes, check_in_tolerance_minutes, min_usage_minutes, reservations_per_space_per_day
) VALUES (1, 120, 15, 75, 1);
