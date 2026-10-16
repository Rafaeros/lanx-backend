package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Só o que o resumo precisa de cada item: evita carregar o item inteiro para somar. */
public record IndicadoresItem(
        BigDecimal valorTotal,
        BigDecimal quantidadeAtendida,
        BigDecimal saldo,
        LocalDate dataEmissaoPc,
        LocalDate dataEntregaPc) {
}
