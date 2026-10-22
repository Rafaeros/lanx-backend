ALTER TABLE cadastros.materiais
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'MATERIA_PRIMA',
    ADD CONSTRAINT ck_materiais_tipo
        CHECK (tipo IN ('MATERIA_PRIMA', 'PRODUTO_ACABADO'));

ALTER TABLE cadastros.operadores
    ADD COLUMN user_id UUID NULL REFERENCES auth.users (id);

-- 1 usuário = no máximo 1 operador ativo (excluídos e operadores sem usuário não contam)
CREATE UNIQUE INDEX uk_operadores_user
    ON cadastros.operadores (user_id)
    WHERE deleted_at IS NULL AND user_id IS NOT NULL;
