CREATE TYPE reservation_status_enum AS ENUM (
  'pending', 'confirmed', 'cancelled_by_admin', 'cancelled_by_user', 'completed'
);

CREATE TYPE role_enum AS ENUM ('student', 'admin');

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  identification VARCHAR(20) NOT NULL UNIQUE,
  first_name VARCHAR(150) NOT NULL,
  last_name VARCHAR(150) NOT NULL,
  password VARCHAR NOT NULL,
  role role_enum NOT NULL DEFAULT 'student'
);

CREATE TABLE space_types (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE spaces (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  capacity INTEGER NOT NULL CHECK (capacity > 0),
  space_type_id BIGINT NOT NULL REFERENCES space_types(id)
);

CREATE INDEX idx_spaces_space_type_id ON spaces(space_type_id);

CREATE TABLE space_schedules (
  id BIGSERIAL PRIMARY KEY,
  space_id BIGINT NOT NULL REFERENCES spaces(id),
  day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  CONSTRAINT chk_space_schedules_time CHECK (end_time > start_time)
);

CREATE INDEX idx_space_schedules_space_id ON space_schedules(space_id);

CREATE TABLE reservations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  qr_code_data VARCHAR NOT NULL UNIQUE,
  user_id UUID NOT NULL REFERENCES users(id),
  space_id BIGINT NOT NULL REFERENCES spaces(id),
  reservation_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  status reservation_status_enum NOT NULL DEFAULT 'pending',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_reservations_time CHECK (end_time > start_time)
);

CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_space_date ON reservations(space_id, reservation_date);

CREATE TABLE reservation_guests (
  id BIGSERIAL PRIMARY KEY,
  reservation_id UUID NOT NULL REFERENCES reservations(id),
  guest_identification VARCHAR(20) NOT NULL,
  guest_name VARCHAR(300)
);

CREATE INDEX idx_reservation_guests_reservation_id ON reservation_guests(reservation_id);
