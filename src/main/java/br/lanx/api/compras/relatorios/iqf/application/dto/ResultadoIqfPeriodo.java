
package br.lanx.api.compras.relatorios.iqf.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResultadoIqfPeriodo(
        LocalDate inicioPeriodo,
        LocalDate fimPeriodo,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo,
        List<ResultadoIqfFornecedor> fornecedores
) {
    public ResultadoIqfPeriodo {
        fornecedores = List.copyOf(fornecedores);
    }
}
