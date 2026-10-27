
package br.lanx.api.qualidade.relatorios.iqf.domain;

import java.math.BigDecimal;
import java.util.Objects;

public enum ClassificacaoIqf {

    A("VERDE"),
    B("AMARELO"),
    C("VERMELHO");

    private static final BigDecimal LIMITE_A = new BigDecimal("84");
    private static final BigDecimal LIMITE_B = new BigDecimal("75");

    private final String cor;

    ClassificacaoIqf(String cor) {
        this.cor = cor;
    }

    public String cor() {
        return cor;
    }

    public static ClassificacaoIqf de(BigDecimal percentual) {
        Objects.requireNonNull(percentual, "O percentual é obrigatório.");

        if (percentual.compareTo(LIMITE_A) >= 0) {
            return A;
        }

        if (percentual.compareTo(LIMITE_B) >= 0) {
            return B;
        }

        return C;
    }
}
