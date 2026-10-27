package br.lanx.api.qualidade.relatorios.iqf.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

import br.lanx.api.qualidade.relatorios.iqf.application.dto.ResultadoIqfFornecedor;
import br.lanx.api.qualidade.relatorios.iqf.domain.MetaIqf;

public record IqfFornecedorResponse(
        UUID fornecedorId,
        String fornecedor,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo,
        ClassificacaoIqfResponse classificacao,
        Boolean atingiuMeta) {

    static IqfFornecedorResponse from(ResultadoIqfFornecedor resultado) {
        return new IqfFornecedorResponse(
                resultado.fornecedorId(),
                resultado.fornecedor(),
                resultado.entregasAvaliadas(),
                resultado.entregasNoPrazo(),
                resultado.entregasAtrasadas(),
                resultado.percentualNoPrazo(),
                ClassificacaoIqfResponse.de(resultado.percentualNoPrazo()),
                MetaIqf.atingiu(resultado.percentualNoPrazo(), MetaIqf.FORNECEDOR));
    }
}
