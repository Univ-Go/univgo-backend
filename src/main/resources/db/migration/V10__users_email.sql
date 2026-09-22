-- Login accepts either the ID number or the institutional email. The sign-in view has
-- always offered both ("Correo institucional o documento"); until now only one existed.
ALTER TABLE users ADD COLUMN email VARCHAR(254);

-- Rows created before the column need a deterministic value before NOT NULL can hold.
-- Derived from the identification so it is unique by construction and obviously a
-- placeholder rather than an address someone might try to deliver mail to.
UPDATE users SET email = identification || '@univgo.edu' WHERE email IS NULL;

ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Case-insensitive: nobody may hold both Ada@x and ada@x, and login matches either way.
CREATE UNIQUE INDEX idx_users_email_lower ON users (lower(email));
