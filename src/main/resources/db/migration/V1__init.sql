CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE
    auth.users (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        username VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

-- name é o identificador funcional da role: MAIÚSCULO com "_" (ADMIN, GERENTE_COMPRAS)
CREATE TABLE
    auth.roles (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        name VARCHAR(50) NOT NULL UNIQUE,
        description VARCHAR(255),
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    auth.user_roles (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, role_id)
    );

CREATE INDEX idx_user_roles_role ON auth.user_roles (role_id);

-- Espelho do catálogo declarado no código ("compras.analise:read"); sincronizado
-- na subida da aplicação. É daqui que o painel monta a tela de permissões.
CREATE TABLE
    auth.permissions (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        code VARCHAR(150) NOT NULL UNIQUE,
        feature_key VARCHAR(100) NOT NULL,
        action VARCHAR(50) NOT NULL,
        description VARCHAR(255) NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW ()
    );

CREATE TABLE
    auth.role_permissions (
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        permission_id UUID NOT NULL REFERENCES auth.permissions (id) ON DELETE CASCADE,
        PRIMARY KEY (role_id, permission_id)
    );

CREATE INDEX idx_role_permissions_permission ON auth.role_permissions (permission_id);

CREATE SCHEMA IF NOT EXISTS cadastros;

CREATE TABLE
    cadastros.unidades_medida (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        nome VARCHAR(100) NOT NULL,
        sigla VARCHAR(10) NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    cadastros.materiais (
        id UUID PRIMARY KEY DEFAULT uuidv7 (),
        codigo VARCHAR(100) NOT NULL UNIQUE,
        descricao VARCHAR(500) NOT NULL,
        unidade_medida_id UUID,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ,
        CONSTRAINT fk_materiais_unidade_medida FOREIGN KEY (unidade_medida_id) REFERENCES cadastros.unidades_medida (id) ON DELETE RESTRICT
    );

CREATE INDEX idx_materiais_unidade_medida_id ON cadastros.materiais (unidade_medida_id);

CREATE INDEX idx_materiais_ativos ON cadastros.materiais (id)
WHERE
    deleted_at IS NULL;