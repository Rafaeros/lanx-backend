package br.lanx.api.compras.relatorios.analisepedidos.domain;

public record IdentificacaoPedido(
    String requisicaoDeCompra,
    String pedidoDeCompra,
    String solicitante,
    String comprador,
    String responsavelCompras
) {}