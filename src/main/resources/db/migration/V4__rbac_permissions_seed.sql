-- Seed permissions and wire them to STUDENT / ADMIN roles.
-- Users (students) are sourced from university enrollment, not managed here:
-- no users:create / users:update / users:deactivate permission exists.

INSERT INTO permissions (name) VALUES
  ('reservations:create'),
  ('reservations:read_own'),
  ('reservations:cancel_own'),
  ('spaces:read'),
  ('users:read_own'),
  ('reservations:read_all'),
  ('reservations:cancel_any'),
  ('reservations:update_status'),
  ('spaces:manage'),
  ('space_types:manage'),
  ('users:read_all'),
  ('roles:manage'),
  ('permissions:manage');

-- STUDENT: own-data scope only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'STUDENT'
  AND p.name IN ('reservations:create','reservations:read_own','reservations:cancel_own','spaces:read','users:read_own');

-- ADMIN: management scope, no user CRUD (students come from enrollment sync)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('reservations:read_all','reservations:cancel_any','reservations:update_status','spaces:manage','space_types:manage','users:read_all','roles:manage','permissions:manage');
