-- Reservation state stops being a stored column and becomes a pure function
-- of these timestamps (spec: "el estado se calcula, no se guarda"). Only the
-- facts a human action actually produces are persisted: creation, check-in,
-- cancellation. Reservada/En curso/Finalizada/Expirada are derived at read
-- time from created_at/checked_in_at/block bounds/now.
ALTER TABLE reservations ADD COLUMN checked_in_at TIMESTAMP NULL;
ALTER TABLE reservations ADD COLUMN cancelled_at TIMESTAMP NULL;

CREATE TYPE reservation_cancelled_by_enum AS ENUM ('student', 'admin');
ALTER TABLE reservations ADD COLUMN cancelled_by reservation_cancelled_by_enum NULL;

ALTER TABLE reservations ADD CONSTRAINT chk_reservations_cancelled_by_with_cancelled_at
  CHECK ((cancelled_at IS NULL) = (cancelled_by IS NULL));

ALTER TABLE reservations DROP COLUMN status;
DROP TYPE reservation_status_enum;

-- Guests don't fit the spec's model (one reservation = exactly one plaza).
DROP TABLE IF EXISTS reservation_guests;
