-- Pedidos de compra podem ser emitidos sem requisição (ex.: amostras)
ALTER TABLE compras.analise_pedido_item
    ALTER COLUMN requisicao_de_compra DROP NOT NULL;
