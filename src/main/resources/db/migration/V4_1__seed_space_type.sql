-- Space type used by V5's hardcoded space_type_id.
-- Fixed id so it matches the literal referenced in V5__seed_spaces_schedules.sql.
-- (Already present in Neon from a manual insert; this makes fresh/local DBs match.)
INSERT INTO space_types (id, name) VALUES
  ('1798287d-6a49-4651-981c-cbefc23b51b5', 'Cancha deportiva')
ON CONFLICT (id) DO NOTHING;
