-- A space stops operating for a stretch of time: maintenance, an incident, an institutional event.
-- Per the functional spec §12 a closure *suspends* — the reservations inside its window keep their
-- place and come back if it is reverted — so nothing here touches the reservations table.
--
-- ends_at NULL is "until somebody reverts it", which is what the panel's maintenance switch
-- creates. reverted_at is what ends a closure without erasing it: that a space was closed three
-- times this month and reopened twice is part of what the panel answers.
CREATE TYPE space_closure_reason_enum AS ENUM (
  'maintenance', 'technical_incident', 'institutional_event', 'external_use', 'other'
);

CREATE TABLE space_closures (
  id           UUID PRIMARY KEY,
  space_id     UUID NOT NULL REFERENCES spaces (id) ON DELETE CASCADE,
  starts_at    TIMESTAMP NOT NULL,
  ends_at      TIMESTAMP NULL,
  reason       space_closure_reason_enum NOT NULL,
  details      TEXT NULL,
  created_by   UUID NOT NULL REFERENCES users (id),
  created_at   TIMESTAMP NOT NULL,
  reverted_at  TIMESTAMP NULL,
  reverted_by  UUID NULL REFERENCES users (id),
  CONSTRAINT chk_space_closures_window CHECK (ends_at IS NULL OR ends_at > starts_at),
  CONSTRAINT chk_space_closures_reverted CHECK ((reverted_at IS NULL) = (reverted_by IS NULL))
);

-- Every read that asks "is this space shut?" filters by space and by still being in force.
CREATE INDEX idx_space_closures_space_in_force ON space_closures (space_id) WHERE reverted_at IS NULL;

-- The maintenance flag becomes a closure with no end date, so that "this space is unavailable" has
-- one meaning instead of two that drift — they already did: the flag hid blocks from students and
-- not from the panel.
--
-- spaces.under_maintenance stays for now and is dropped in a later migration: this is the expand
-- half of an expand/contract, and the database is shared.
INSERT INTO space_closures (id, space_id, starts_at, reason, details, created_by, created_at)
SELECT
  gen_random_uuid(),
  s.id,
  NOW(),
  'maintenance',
  'Migrado desde spaces.under_maintenance',
  (SELECT u.id
     FROM users u
     JOIN user_roles ur ON ur.user_id = u.id
     JOIN roles r ON r.id = ur.role_id
    WHERE r.name = 'ADMIN'
    ORDER BY u.id
    LIMIT 1),
  NOW()
FROM spaces s
WHERE s.under_maintenance = true;
