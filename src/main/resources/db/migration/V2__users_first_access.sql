-- Primeiro acesso: o usuário cadastrado pelo administrador recebe uma senha
-- provisória e precisa definir a própria antes de entrar no sistema.
-- Usuários que já existem continuam entrando normalmente (false).
ALTER TABLE auth.users
    ADD COLUMN first_access BOOLEAN NOT NULL DEFAULT FALSE;
