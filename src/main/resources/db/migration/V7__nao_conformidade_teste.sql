CREATE SCHEMA IF NOT EXISTS operacoes;

CREATE SCHEMA IF NOT EXISTS cadastros;

CREATE TABLE
    cadastros.tipo_nao_conformidade (
        id UUID PRIMARY KEY,
        nome VARCHAR(200) NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE UNIQUE INDEX ux_tipo_nao_conformidade_nome ON cadastros.tipo_nao_conformidade (LOWER(nome))
WHERE
    deleted_at IS NULL;

CREATE TABLE
    operacoes.nao_conformidades (
        id UUID PRIMARY KEY,
        tipo_nao_conformidade_id UUID NOT NULL,
        departamento_id UUID NOT NULL,
        ordem_de_producao_id UUID NOT NULL,
        operador_id UUID NULL,
        quantidade_nao_conforme DECIMAL(19, 4) NOT NULL,
        data_nao_conformidade DATE NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ,
        CONSTRAINT fk_nc_tipo FOREIGN KEY (tipo_nao_conformidade_id) REFERENCES cadastros.tipo_nao_conformidade (id),
        CONSTRAINT fk_nc_departamento FOREIGN KEY (departamento_id) REFERENCES cadastros.departamentos (id),
        CONSTRAINT fk_nc_ordem_producao FOREIGN KEY (ordem_de_producao_id) REFERENCES operacoes.ordens_de_producao (id),
        CONSTRAINT fk_nc_operador FOREIGN KEY (operador_id) REFERENCES cadastros.operadores (id),
        CONSTRAINT ck_nc_quantidade CHECK (quantidade_nao_conforme > 0)
    );

CREATE INDEX idx_nc_tipo ON operacoes.nao_conformidades (tipo_nao_conformidade_id);

CREATE INDEX idx_nc_departamento ON operacoes.nao_conformidades (departamento_id);

CREATE INDEX idx_nc_ordem_producao ON operacoes.nao_conformidades (ordem_de_producao_id);

CREATE INDEX idx_nc_operador ON operacoes.nao_conformidades (operador_id);

CREATE TABLE
    operacoes.nao_conformidade_anexos (
        id UUID PRIMARY KEY,
        nao_conformidade_id UUID NOT NULL,
        nome_arquivo VARCHAR(255) NOT NULL,
        chave_armazenamento VARCHAR(1000) NOT NULL,
        content_type VARCHAR(100) NOT NULL,
        tamanho_bytes BIGINT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_nc_anexo_nao_conformidade FOREIGN KEY (nao_conformidade_id) REFERENCES operacoes.nao_conformidades (id) ON DELETE CASCADE,
        CONSTRAINT uk_nc_anexo_chave_armazenamento UNIQUE (chave_armazenamento),
        CONSTRAINT ck_nc_anexo_tamanho CHECK (tamanho_bytes > 0)
    );

CREATE INDEX idx_nc_anexo_nao_conformidade ON operacoes.nao_conformidade_anexos (nao_conformidade_id);