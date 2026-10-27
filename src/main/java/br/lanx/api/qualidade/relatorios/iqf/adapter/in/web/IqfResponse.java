package br.lanx.api.qualidade.relatorios.iqf.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.lanx.api.qualidade.relatorios.iqf.application.dto.RateioIqf;
import br.lanx.api.qualidade.relatorios.iqf.application.dto.ResultadoIqfPeriodo;
import br.lanx.api.qualidade.relatorios.iqf.domain.MetaIqf;

/**
 * IQF de prazo do período. {@code percentualNoPrazo} e {@code classificacao}
 * ficam nulos quando não há entregas avaliadas (só pendentes ou sem previsão).
 * {@code meses} traz todos os meses do período, em ordem, mesmo os vazios.
 * {@code metaMensal} é a meta usada nos meses: 97% no geral, 85% na consulta
 * de um fornecedor.
 */
public record IqfResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        /** Rateios filtrados (vazio = todos). */
        List<RateioResumo> rateios,
        int toleranciaDias,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo,
        ClassificacaoIqfResponse classificacao,
        List<IqfFornecedorResponse> fornecedores,
        List<IqfMesResponse> meses,
        BigDecimal metaMensal) {

    public record RateioResumo(UUID id, String nome) {
        static RateioResumo from(RateioIqf rateio) {
            return new RateioResumo(rateio.id(), rateio.nome());
        }
    }

    /** IQF geral: meses avaliados contra a meta de 97%. */
    static IqfResponse from(ResultadoIqfPeriodo resultado) {
        return from(resultado, MetaIqf.GERAL_MENSAL);
    }

    /** IQF de um fornecedor: meses avaliados contra a meta de 85%. */
    static IqfResponse doFornecedor(ResultadoIqfPeriodo resultado) {
        return from(resultado, MetaIqf.FORNECEDOR);
    }

    private static IqfResponse from(ResultadoIqfPeriodo resultado, BigDecimal meta) {
        return new IqfResponse(
                resultado.inicioPeriodo(),
                resultado.fimPeriodo(),
                resultado.rateios().stream().map(RateioResumo::from).toList(),
                resultado.toleranciaDias(),
                resultado.entregasAvaliadas(),
                resultado.entregasNoPrazo(),
                resultado.entregasAtrasadas(),
                resultado.percentualNoPrazo(),
                ClassificacaoIqfResponse.de(resultado.percentualNoPrazo()),
                resultado.fornecedores().stream().map(IqfFornecedorResponse::from).toList(),
                resultado.meses().stream().map(m -> IqfMesResponse.from(m, meta)).toList(),
                meta);
    }
}
