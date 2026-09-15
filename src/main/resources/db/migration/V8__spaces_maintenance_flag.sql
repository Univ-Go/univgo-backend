-- Lets admins pull a space out of the catalog for unplanned maintenance
-- ("El espacio está en mantenimiento" case in the functional spec).
ALTER TABLE spaces ADD COLUMN under_maintenance BOOLEAN NOT NULL DEFAULT false;
