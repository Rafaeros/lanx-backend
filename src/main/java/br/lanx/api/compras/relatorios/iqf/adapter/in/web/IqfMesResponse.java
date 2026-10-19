package br.lanx.api.compras.relatorios.iqf.adapter.in.web;

import java.math.BigDecimal;

import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfMes;
import br.lanx.api.compras.relatorios.iqf.domain.MetaIqf;

/**
 * IQF de um mês ("2026-09"); percentual, classificação e atingiuMeta ausentes
 * quando nada foi avaliado.
 */
public record IqfMesResponse(
        String mes,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo,
        ClassificacaoIqfResponse classificacao,
        Boolean atingiuMeta) {

    static IqfMesResponse from(ResultadoIqfMes resultado, BigDecimal meta) {
        return new IqfMesResponse(
                resultado.mes().toString(),
                resultado.entregasAvaliadas(),
                resultado.entregasNoPrazo(),
                resultado.entregasAtrasadas(),
                resultado.percentualNoPrazo(),
                ClassificacaoIqfResponse.de(resultado.percentualNoPrazo()),
                MetaIqf.atingiu(resultado.percentualNoPrazo(), meta));
    }
}
