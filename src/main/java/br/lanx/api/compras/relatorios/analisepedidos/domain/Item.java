package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.math.BigDecimal;

public record Item(
        String codigo,
        String descricao,
        String unidadeMedida,
        BigDecimal quantidadeSolicitada) {
}