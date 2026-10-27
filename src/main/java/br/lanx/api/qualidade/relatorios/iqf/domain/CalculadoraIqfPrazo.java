package br.lanx.api.qualidade.relatorios.iqf.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculadoraIqfPrazo {
    private CalculadoraIqfPrazo() {
    }

    public static Iqf calcular(
            long entregasNoPrazo,
            long entregasAvaliadas) {
        if (entregasAvaliadas <= 0) {
            throw new IllegalArgumentException(
                    "É necessário haver entregas avaliadas.");
        }

        if (entregasNoPrazo < 0
                || entregasNoPrazo > entregasAvaliadas) {
            throw new IllegalArgumentException(
                    "Quantidade de entregas inválida.");
        }

        BigDecimal percentual = BigDecimal
                .valueOf(entregasNoPrazo)
                .multiply(new BigDecimal("100"))
                .divide(
                        BigDecimal.valueOf(entregasAvaliadas),
                        2,
                        RoundingMode.HALF_UP);

        return new Iqf(percentual);
    }
}
