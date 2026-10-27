package br.lanx.api.qualidade.relatorios.iqf.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.lanx.api.qualidade.relatorios.iqf.application.dto.RateioIqf;
import br.lanx.api.qualidade.relatorios.iqf.application.dto.ResultadoIqfPeriodo;

public interface ConsultarIqfUseCase {

    /**
     * IQF do período, geral, por fornecedor e mês a mês. {@code rateioIds} vazio
     * considera todos os rateios; {@code toleranciaDias} = dias de atraso após a
     * previsão que ainda contam como no prazo (0 a 30).
     */
    ResultadoIqfPeriodo consultar(LocalDate inicioPeriodo, LocalDate fimPeriodo, Set<UUID> rateioIds,
            int toleranciaDias);

    /**
     * O mesmo cálculo, só com as entregas de um fornecedor (série mensal dele).
     * Sem entregas no período, volta zerado.
     */
    ResultadoIqfPeriodo consultarFornecedor(UUID fornecedorId, LocalDate inicioPeriodo, LocalDate fimPeriodo,
            Set<UUID> rateioIds, int toleranciaDias);

    /** Rateios existentes nos itens importados, por nome, para o filtro da tela. */
    List<RateioIqf> listarRateios();
}
