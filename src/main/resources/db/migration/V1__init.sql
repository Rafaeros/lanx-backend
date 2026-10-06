CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.users (
    id            UUID         PRIMARY KEY DEFAULT uuidv7(),
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE auth.roles (
    id   UUID        PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE auth.user_roles (
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_roles_role ON auth.user_roles(role_id);

CREATE TABLE auth.role_permissions (
    role_id UUID        NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
    feature VARCHAR(30) NOT NULL,
    mask    INTEGER     NOT NULL CHECK (mask >= 0),
    PRIMARY KEY (role_id, feature)
);