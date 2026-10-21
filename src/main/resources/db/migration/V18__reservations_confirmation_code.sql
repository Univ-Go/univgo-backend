-- A 6-digit code the admin can type when the QR does not scan. It only has to tell apart the
-- reservations of one day: the door is always checked against today's bookings, so the same code
-- may come back on another date. A cancelled reservation gives its code back.
-- Text, not a number, so leading zeros survive ("004217"). Nullable: rows booked before this
-- migration keep working through their QR only.
ALTER TABLE reservations ADD COLUMN confirmation_code VARCHAR(6);

CREATE UNIQUE INDEX reservations_confirmation_code_per_day
    ON reservations (reservation_date, confirmation_code)
    WHERE cancelled_at IS NULL;
