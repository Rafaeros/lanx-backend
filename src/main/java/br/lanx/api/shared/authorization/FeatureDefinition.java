package br.lanx.api.shared.authorization;

import java.util.Set;

/**
 * Uma funcionalidade declarada por um módulo. Além do CRUD (bitmask), pode ter
 * ações extras nomeadas, como "anexar-arquivo" ou "aprovar". Relatórios não
 * têm CRUD: só leitura e as ações extras ({@link #report}).
 */
public record FeatureDefinition(FeatureKey key, String label, Set<String> extraActions, boolean crud) {

    public FeatureDefinition {
        if (key == null || label == null || label.isBlank()) {
            throw new InvalidFeatureKeyException("Funcionalidade precisa de chave e rótulo");
        }
        extraActions = Set.copyOf(extraActions);
    }

    public static FeatureDefinition of(String key, String label, String... extraActions) {
        return new FeatureDefinition(FeatureKey.of(key), label, Set.of(extraActions), true);
    }

    /** Funcionalidade somente leitura (relatório): "read" + as ações extras. */
    public static FeatureDefinition report(String key, String label, String... extraActions) {
        return new FeatureDefinition(FeatureKey.of(key), label, Set.of(extraActions), false);
    }
}
