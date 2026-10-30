package br.lanx.api.cadastros.tiponaoconformidade;

import java.util.UUID;

/**
 * Quem usa tipos de não conformidade (ex.: as não conformidades de teste e inspeção)
 * implementa isto para vetar a exclusão de um tipo em uso. O tipo não conhece
 * os módulos que o usam.
 */
public interface TipoNaoConformidadeUsageCheck {
    boolean isInUse(UUID tipoNaoConformidadeId);
}
