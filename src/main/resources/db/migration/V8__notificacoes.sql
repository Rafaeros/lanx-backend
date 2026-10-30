CREATE SCHEMA IF NOT EXISTS notificacoes;

CREATE TABLE
    notificacoes.notificacoes (
        id UUID PRIMARY KEY,
        user_id UUID NOT NULL REFERENCES auth.users (id),
        tipo VARCHAR(50) NOT NULL,
        titulo VARCHAR(150) NOT NULL,
        mensagem VARCHAR(500) NOT NULL,
        link VARCHAR(255),
        referencia_id UUID,
        lida_em TIMESTAMPTZ,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        CONSTRAINT uk_notificacao_user_tipo_ref UNIQUE (user_id, tipo, referencia_id)
    );

CREATE INDEX idx_notificacoes_user_nao_lidas ON notificacoes.notificacoes (user_id, created_at DESC)
WHERE
    lida_em IS NULL;