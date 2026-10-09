CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE
    auth.users (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        username VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    auth.roles (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        name VARCHAR(50) NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    auth.user_roles (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, role_id)
    );

CREATE INDEX idx_user_roles_role ON auth.user_roles (role_id);

-- Permissão CRUD da role por funcionalidade (bitmask). A chave é um caminho
-- ("compras.analise"): o catálogo de funcionalidades vive no código, não no banco.
CREATE TABLE
    auth.role_permissions (
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        feature VARCHAR(100) NOT NULL,
        mask INTEGER NOT NULL CHECK (mask >= 0),
        PRIMARY KEY (role_id, feature)
    );

-- Ações extras nomeadas da role ("compras.analise" + "aprovar")
CREATE TABLE
    auth.role_actions (
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        feature VARCHAR(100) NOT NULL,
        action VARCHAR(50) NOT NULL,
        PRIMARY KEY (role_id, feature, action)
    );

-- Exceções individuais do usuário: grants somam à role, denies subtraem (deny vence)
CREATE TABLE
    auth.user_grants (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        feature VARCHAR(100) NOT NULL,
        mask INTEGER NOT NULL CHECK (mask >= 0),
        PRIMARY KEY (user_id, feature)
    );

CREATE TABLE
    auth.user_denies (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        feature VARCHAR(100) NOT NULL,
        mask INTEGER NOT NULL CHECK (mask >= 0),
        PRIMARY KEY (user_id, feature)
    );

CREATE TABLE
    auth.user_grant_actions (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        feature VARCHAR(100) NOT NULL,
        action VARCHAR(50) NOT NULL,
        PRIMARY KEY (user_id, feature, action)
    );

CREATE TABLE
    auth.user_deny_actions (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        feature VARCHAR(100) NOT NULL,
        action VARCHAR(50) NOT NULL,
        PRIMARY KEY (user_id, feature, action)
    );

CREATE SCHEMA IF NOT EXISTS cadastros;

CREATE TABLE
    cadastros.unidades_medida (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        nome VARCHAR(100) NOT NULL,
        sigla VARCHAR(10) NOT NULL UNIQUE,
        fator_conversao DECIMAL(19,4) NOT NULL DEFAULT 1,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        deleted_at TIMESTAMPTZ
    );