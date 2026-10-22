-- Comprador e rateio dos itens importados passam a ser cadastros (V2): cria os cadastros
-- a partir dos textos que já estavam nos itens, liga cada item pelo nome e remove o texto.

-- 1. Cadastros a partir dos textos (sem diferenciar maiúsculas nem espaços nas pontas)
INSERT INTO cadastros.compradores (id, nome)
SELECT uuidv7(), MIN(TRIM(comprador))
FROM compras.analise_pedidos_itens
WHERE comprador IS NOT NULL AND TRIM(comprador) <> ''
GROUP BY lower(TRIM(comprador))
ON CONFLICT DO NOTHING;

INSERT INTO cadastros.rateios (id, nome)
SELECT uuidv7(), MIN(TRIM(rateio))
FROM compras.analise_pedidos_itens
WHERE rateio IS NOT NULL AND TRIM(rateio) <> ''
GROUP BY lower(TRIM(rateio))
ON CONFLICT DO NOTHING;

-- 2. Cada item aponta para o cadastro com o mesmo nome
UPDATE compras.analise_pedidos_itens i
SET comprador_id = c.id
FROM cadastros.compradores c
WHERE lower(TRIM(i.comprador)) = lower(c.nome) AND c.deleted_at IS NULL;

UPDATE compras.analise_pedidos_itens i
SET rateio_id = r.id
FROM cadastros.rateios r
WHERE lower(TRIM(i.rateio)) = lower(r.nome) AND r.deleted_at IS NULL;

-- 3. O texto vira redundante
ALTER TABLE compras.analise_pedidos_itens
    DROP COLUMN comprador,
    DROP COLUMN rateio;
