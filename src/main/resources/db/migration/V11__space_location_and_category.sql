-- The catalog groups spaces by category and lets a student search by where a space is; neither was
-- on record.
--
-- The category hangs off space_types and not off spaces because space_types already IS the
-- taxonomy: putting it on the space row would let two rooms of the same type disagree on what
-- they are.

ALTER TABLE space_types ADD COLUMN category VARCHAR(20);

UPDATE space_types SET category = 'SPORTS' WHERE category IS NULL;

ALTER TABLE space_types
  ALTER COLUMN category SET NOT NULL,
  ADD CONSTRAINT chk_space_types_category CHECK (category IN ('SPORTS', 'STUDY', 'LAB'));

ALTER TABLE spaces ADD COLUMN location VARCHAR(150);

-- The six existing spaces are courts of the same complex (V5). This value is a deliberate
-- placeholder: NOT NULL needs a deterministic value for rows that predate the column.
UPDATE spaces SET location = 'Complejo Deportivo Central' WHERE location IS NULL;

ALTER TABLE spaces ALTER COLUMN location SET NOT NULL;
