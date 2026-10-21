package br.lanx.api.shared.authorization;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Uma funcionalidade declarada por um módulo. Além do CRUD (bitmask), pode ter
 * ações extras nomeadas, como "anexar-arquivo" ou "aprovar". Relatórios não
 * têm CRUD: só leitura e as ações extras ({@link #report}).
 */
public record FeatureDefinition(FeatureKey key, String label, Set<String> extraActions, boolean crud) {

    /** Só o nome da ação ("importar"), nunca o código completo ("compras.x:importar"). */
    private static final Pattern ACTION_FORMAT = Pattern.compile("^[a-z][a-z0-9-]{0,49}$");

    public FeatureDefinition {
        if (key == null || label == null || label.isBlank()) {
            throw new InvalidFeatureKeyException("Funcionalidade precisa de chave e rótulo");
        }
        extraActions = Set.copyOf(extraActions);
        for (String action : extraActions) {
            if (!ACTION_FORMAT.matcher(action).matches()) {
                throw new InvalidFeatureKeyException("Ação extra inválida em '" + key + "': '" + action
                        + "' (use só o nome da ação, ex.: \"importar\", não o código completo)");
            }
        }
    }

    public static FeatureDefinition of(String key, String label, String... extraActions) {
        return new FeatureDefinition(FeatureKey.of(key), label, Set.of(extraActions), true);
    }

    /** Funcionalidade somente leitura (relatório): "read" + as ações extras. */
    public static FeatureDefinition report(String key, String label, String... extraActions) {
        return new FeatureDefinition(FeatureKey.of(key), label, Set.of(extraActions), false);
    }
}
