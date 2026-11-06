CREATE SCHEMA IF NOT EXISTS engenharia;

CREATE TABLE engenharia.desenho_jobs (
    id               UUID PRIMARY KEY,
    solicitante_id   UUID NOT NULL REFERENCES auth.users (id),
    projeto          VARCHAR(100) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
    -- {"formatos":["a4","painel"],"acabamento":"F","vistas":true,"biblioteca":true,"dwg":true}
    opcoes           JSONB NOT NULL DEFAULT '{}'::jsonb,
    -- carimbo/topologia revisados no front (mesclados no project.json pelo worker)
    project_override JSONB,
    pendencias       JSONB NOT NULL DEFAULT '[]'::jsonb,
    erro             TEXT,
    tentativas       INT    NOT NULL DEFAULT 1,
    version          BIGINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,
    CONSTRAINT ck_desenho_job_status CHECK (status IN ('PENDENTE', 'PROCESSANDO', 'CONCLUIDO', 'ERRO'))
);
CREATE INDEX idx_desenho_job_solicitante ON engenharia.desenho_jobs (solicitante_id, created_at DESC);
CREATE INDEX idx_desenho_job_projeto     ON engenharia.desenho_jobs (projeto);
CREATE INDEX idx_desenho_job_em_andamento ON engenharia.desenho_jobs (created_at)
    WHERE status IN ('PENDENTE', 'PROCESSANDO');

CREATE TABLE engenharia.desenho_job_arquivos (
    id                  UUID PRIMARY KEY,
    desenho_job_id      UUID NOT NULL REFERENCES engenharia.desenho_jobs (id) ON DELETE CASCADE,
    tipo                VARCHAR(20)   NOT NULL,
    nome_arquivo        VARCHAR(255)  NOT NULL,
    chave_armazenamento VARCHAR(1000) NOT NULL,
    content_type        VARCHAR(100)  NOT NULL,
    tamanho_bytes       BIGINT        NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_desenho_arquivo_chave    UNIQUE (chave_armazenamento),
    CONSTRAINT uk_desenho_arquivo_job_tipo UNIQUE (desenho_job_id, tipo),
    CONSTRAINT ck_desenho_arquivo_tipo     CHECK (tipo IN ('LISTA_CORTE', 'DESENHO_CLIENTE', 'RESULTADO_ZIP')),
    CONSTRAINT ck_desenho_arquivo_tamanho  CHECK (tamanho_bytes > 0)
);
CREATE INDEX idx_desenho_arquivo_job ON engenharia.desenho_job_arquivos (desenho_job_id);