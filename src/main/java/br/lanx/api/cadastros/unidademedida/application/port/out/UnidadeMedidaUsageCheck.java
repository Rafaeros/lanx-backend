package br.lanx.api.cadastros.unidademedida.application.port.out;

import java.util.UUID;

/**
 * Quem usa unidades de medida (ex.: materiais) implementa isto para vetar a
 * exclusão de uma unidade em uso. A unidade não conhece os módulos que a usam:
 * eles é que dependem dela, então não há ciclo.
 */
public interface UnidadeMedidaUsageCheck {
    boolean isInUse(UUID unidadeMedidaId);
}
