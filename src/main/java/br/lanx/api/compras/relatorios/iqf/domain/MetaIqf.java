package br.lanx.api.compras.relatorios.iqf.domain;

import java.math.BigDecimal;

/**
 * Metas do IQF de prazo: o geral precisa de 97% das entregas no prazo em cada
 * mês; cada fornecedor, de 85%.
 */
public final class MetaIqf {

    public static final BigDecimal GERAL_MENSAL = new BigDecimal("97");
    public static final BigDecimal FORNECEDOR = new BigDecimal("85");

    private MetaIqf() {
    }

    /** Percentual nulo (nada avaliado) não atinge nem perde a meta. */
    public static Boolean atingiu(BigDecimal percentual, BigDecimal meta) {
        return percentual == null ? null : percentual.compareTo(meta) >= 0;
    }
}
