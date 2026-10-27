package br.lanx.api.qualidade.relatorios.iqf.adapter.in.web;

import java.math.BigDecimal;

import br.lanx.api.qualidade.relatorios.iqf.domain.ClassificacaoIqf;

/** Faixa do IQF: A/VERDE (>= 84%), B/AMARELO (>= 75%) ou C/VERMELHO. */
public record ClassificacaoIqfResponse(String faixa, String cor) {

    static ClassificacaoIqfResponse de(BigDecimal percentual) {
        if (percentual == null) {
            return null;
        }
        ClassificacaoIqf classificacao = ClassificacaoIqf.de(percentual);
        return new ClassificacaoIqfResponse(classificacao.name(), classificacao.cor());
    }
}
