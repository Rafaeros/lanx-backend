
package br.lanx.api.compras.relatorios.iqf.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ResultadoIqfPeriodo(
        LocalDate inicioPeriodo,
        LocalDate fimPeriodo,
        Set<String> rateios,
        int toleranciaDias,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo,
        List<ResultadoIqfFornecedor> fornecedores,
        List<ResultadoIqfMes> meses
) {
    public ResultadoIqfPeriodo {
        rateios = Set.copyOf(rateios);
        fornecedores = List.copyOf(fornecedores);
        meses = List.copyOf(meses);
    }
}
