-- The bucket stopped being able to answer what the product needs to know about a space's
-- photographs. It knows which files exist, but not which one is the cover: order came from S3's
-- lastModified, so the cover was whichever file happened to be uploaded first and an administrator
-- had no way to say otherwise. Explicit order needs a row.
--
-- The derivative keys are derived from the ids rather than stored one per size:
--   spaces/{space_id}/{id}/{width}.jpg
-- so adding a width to univgo.spaces.images.widths is a backfill job and not a schema change.
CREATE TABLE space_images (
  id           UUID        PRIMARY KEY,
  space_id     UUID        NOT NULL REFERENCES spaces (id) ON DELETE CASCADE,
  position     SMALLINT    NOT NULL,
  original_key TEXT        NOT NULL UNIQUE,
  content_type VARCHAR(50) NOT NULL,
  width        INTEGER     NOT NULL,
  height       INTEGER     NOT NULL,
  byte_size    INTEGER     NOT NULL,
  created_at   TIMESTAMP   NOT NULL,
  created_by   UUID        NOT NULL REFERENCES users (id),
  CONSTRAINT chk_space_images_position CHECK (position >= 0),
  CONSTRAINT chk_space_images_dimensions CHECK (width > 0 AND height > 0 AND byte_size > 0),
  -- DEFERRABLE on purpose. Any reorder passes, inside one transaction, through a state where two
  -- rows share a position. Without deferring, a reorder would have to shuffle in two phases
  -- through negative positions, which is the kind of workaround that gets copied and then broken.
  CONSTRAINT space_images_position_unique UNIQUE (space_id, position) DEFERRABLE INITIALLY DEFERRED
);

-- The catalogue reads a space's photographs in order, always by space.
CREATE INDEX idx_space_images_space_position ON space_images (space_id, position);

-- "Delete a space" means archive it. A retired space leaves the catalogue and the panel, but its
-- reservations are history and reservations.space_id has no ON DELETE CASCADE: a hard DELETE would
-- either fail on the foreign key or, with a cascade bolted on, erase what happened.
--
-- Same shape as space_closures.reverted_at — a nullable instant, so the column records *when* and
-- every filter is IS NULL — rather than a boolean, which records nothing. Archiving also opens an
-- indefinite closure, so an archived space's reservations read as suspended (spec §12) instead of
-- silently vanishing; that is the service's job, not this migration's.
ALTER TABLE spaces
  ADD COLUMN archived_at TIMESTAMP NULL,
  ADD COLUMN archived_by UUID NULL REFERENCES users (id),
  ADD CONSTRAINT chk_spaces_archived CHECK ((archived_at IS NULL) = (archived_by IS NULL));

-- Every catalogue read filters on this, and an archived space is the rare case.
CREATE INDEX idx_spaces_active ON spaces (id) WHERE archived_at IS NULL;
