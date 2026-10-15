package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.math.BigDecimal;

public record Entrega(
        BigDecimal quantidadeAtendida,
        String local,
        String rateio,
        String referencia) {
}
