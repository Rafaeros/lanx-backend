package br.lanx.api.compras.relatorios.iqf.domain;

import java.math.BigDecimal;

public record Iqf(
        BigDecimal percentual) {
    public Iqf {
        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) < 0
                || percentual.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("O IQF deve estar entre 0 e 100");
        }
    }

    public ClassificacaoIqf classificacao() {
        return ClassificacaoIqf.de(percentual);
    }
}
