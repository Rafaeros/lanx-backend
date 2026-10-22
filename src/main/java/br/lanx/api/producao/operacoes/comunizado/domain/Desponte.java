package br.lanx.api.producao.operacoes.comunizado.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Desponte (decapagem) da ponta do cabo, como texto no formato do comunizado: cada
 * medida com uma casa e ponto ("3.5", "6.0"). Cabos blindados/trançados trazem duas
 * medidas separadas por barra ("40.0/6.0": capa externa / condutor), que são mantidas.
 */
public final class Desponte {

    private Desponte() {
    }

    /**
     * "6" → "6.0", "3,5" → "3.5", "40/6" → "40.0/6.0". Vazio → null.
     *
     * @throws IllegalArgumentException se alguma parte não for um número não negativo
     */
    public static String normalizar(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            return null;
        }
        List<String> medidas = new ArrayList<>();
        for (String parte : bruto.split("/", -1)) {
            medidas.add(formatar(numero(parte.trim(), bruto)));
        }
        return String.join("/", medidas);
    }

    private static BigDecimal numero(String parte, String bruto) {
        try {
            BigDecimal valor = new BigDecimal(parte.replace(",", "."));
            if (valor.signum() < 0) {
                throw new IllegalArgumentException("Desponte negativo: " + bruto);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Desponte inválido: " + bruto, e);
        }
    }

    private static String formatar(BigDecimal valor) {
        BigDecimal limpo = valor.stripTrailingZeros();
        return (limpo.scale() < 1 ? limpo.setScale(1) : limpo).toPlainString();
    }
}
