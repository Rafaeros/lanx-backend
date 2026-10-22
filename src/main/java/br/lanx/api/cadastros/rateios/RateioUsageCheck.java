package br.lanx.api.cadastros.rateios;

import java.util.UUID;

/**
 * Quem usa rateios (ex.: itens da análise de pedidos) implementa isto para vetar
 * a exclusão de um rateio em uso. O rateio não conhece os módulos que o usam.
 */
public interface RateioUsageCheck {
    boolean isInUse(UUID rateioId);
}
