
package br.lanx.api.qualidade.relatorios.iqf.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResultadoIqfPeriodo(
        LocalDate inicioPeriodo,
        LocalDate fimPeriodo,
        /** Rateios filtrados, por nome; vazio = todos. */
        List<RateioIqf> rateios,
        int toleranciaDias,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo,
        List<ResultadoIqfFornecedor> fornecedores,
        List<ResultadoIqfMes> meses
) {
    public ResultadoIqfPeriodo {
        rateios = List.copyOf(rateios);
        fornecedores = List.copyOf(fornecedores);
        meses = List.copyOf(meses);
    }
}
