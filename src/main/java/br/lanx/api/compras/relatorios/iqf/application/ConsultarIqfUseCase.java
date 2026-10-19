package br.lanx.api.compras.relatorios.iqf.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfPeriodo;

public interface ConsultarIqfUseCase {

    /**
     * IQF do período, geral, por fornecedor e mês a mês. {@code rateios} vazio
     * considera todos os rateios; {@code toleranciaDias} = dias de atraso após a
     * previsão que ainda contam como no prazo (0 a 30).
     */
    ResultadoIqfPeriodo consultar(LocalDate inicioPeriodo, LocalDate fimPeriodo, Set<String> rateios,
            int toleranciaDias);

    /**
     * O mesmo cálculo, só com as entregas de um fornecedor (série mensal dele).
     * Sem entregas no período, volta zerado.
     */
    ResultadoIqfPeriodo consultarFornecedor(UUID fornecedorId, LocalDate inicioPeriodo, LocalDate fimPeriodo,
            Set<String> rateios, int toleranciaDias);

    /** Rateios existentes nos itens importados, para o filtro da tela. */
    List<String> listarRateios();
}
