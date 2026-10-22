CREATE TABLE cadastros.rateios (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
    deleted_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX ux_rateios_nome ON cadastros.rateios(lower(nome)) WHERE deleted_at IS NULL;

CREATE TABLE cadastros.compradores(
    id UUID PRIMARY KEY,
    user_id UUID NULL REFERENCES auth.users(id),
    nome VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
    deleted_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX ux_compradores_nome ON cadastros.compradores(lower(nome)) WHERE deleted_at IS NULL;


ALTER TABLE compras.analise_pedidos_itens 
    ADD COLUMN comprador_id UUID NULL REFERENCES cadastros.compradores(id),
    ADD COLUMN rateio_id UUID NULL REFERENCES cadastros.rateios(id);

CREATE INDEX idx_analise_itens_comprador_id ON compras.analise_pedidos_itens(comprador_id);
CREATE INDEX idx_analise_itens_rateio_id ON compras.analise_pedidos_itens(rateio_id);