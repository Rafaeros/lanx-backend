CREATE TABLE
    cadastros.clientes (
        id UUID PRIMARY KEY,
        nome VARCHAR(250) NOT NULL,
        cnpj VARCHAR(14) NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE SCHEMA IF NOT EXISTS operacoes;

CREATE TABLE
    operacoes.ordens_de_producao (
        id UUID PRIMARY KEY,
        cliente_id UUID NOT NULL,
        material_id UUID NOT NULL,
        codigo VARCHAR(10) NOT NULL UNIQUE,
        quantidade DECIMAL(19, 4) NOT NULL,
        previsao DATE NOT NULL,
        etapa_atual VARCHAR(100) NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ,
        CONSTRAINT chk_op_codigo CHECK (LENGTH(TRIM(codigo)) > 0),
        CONSTRAINT chk_op_quantidade CHECK (quantidade > 0),
        CONSTRAINT fk_op_cliente FOREIGN KEY (cliente_id) REFERENCES cadastros.clientes (id) ON DELETE RESTRICT,
        CONSTRAINT fk_op_material FOREIGN KEY (material_id) REFERENCES cadastros.materiais (id) ON DELETE RESTRICT
    );

CREATE INDEX idx_op_cliente ON operacoes.ordens_de_producao (cliente_id);

CREATE INDEX idx_op_material ON operacoes.ordens_de_producao (material_id);

CREATE INDEX idx_op_previsao ON operacoes.ordens_de_producao (previsao);

CREATE INDEX idx_op_ativas ON operacoes.ordens_de_producao (created_at)
WHERE
    deleted_at IS NULL;