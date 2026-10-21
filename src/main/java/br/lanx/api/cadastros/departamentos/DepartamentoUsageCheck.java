package br.lanx.api.cadastros.departamentos;

import java.util.UUID;

/**
 * Quem usa departamentos (ex.: operadores) implementa isto para vetar a
 * exclusão de um departamento em uso. O departamento não conhece os módulos que
 * o usam: eles é que dependem dele, então não há ciclo.
 */
public interface DepartamentoUsageCheck {
    boolean isInUse(UUID departamentoId);
}
