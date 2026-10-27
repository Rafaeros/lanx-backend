-- Unicidade só entre registros ativos (mesmo padrão de rateios/compradores):
-- um cliente ou OP excluído logicamente libera o CNPJ/código para um novo cadastro.
ALTER TABLE cadastros.clientes DROP CONSTRAINT clientes_cnpj_key;
CREATE UNIQUE INDEX ux_clientes_cnpj ON cadastros.clientes (cnpj) WHERE deleted_at IS NULL AND cnpj IS NOT NULL;
CREATE UNIQUE INDEX ux_clientes_nome ON cadastros.clientes (lower(nome)) WHERE deleted_at IS NULL;

ALTER TABLE operacoes.ordens_de_producao DROP CONSTRAINT ordens_de_producao_codigo_key;
CREATE UNIQUE INDEX ux_op_codigo ON operacoes.ordens_de_producao (lower(codigo)) WHERE deleted_at IS NULL;
