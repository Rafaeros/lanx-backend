package br.lanx.api.compras.relatorios.iqf.application.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

/** IQF de um mês do período (pela previsão de entrega). Percentual nulo = nada avaliado no mês. */
public record ResultadoIqfMes(
        YearMonth mes,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo) {
}
