package br.lanx.api.auth.permission.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.lanx.api.auth.permission.domain.exception.UnknownFeatureException;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

/**
 * Catálogo de funcionalidades do sistema, montado em memória a partir do que
 * cada módulo declara. Não existe tabela nem seeder: declarou no código, existe.
 * A ordem de declaração é preservada (vira a ordem do menu).
 */
public final class FeatureCatalog {

    private final Map<FeatureKey, FeatureDefinition> byKey;

    private FeatureCatalog(Map<FeatureKey, FeatureDefinition> byKey) {
        this.byKey = byKey;
    }

    /** Erros aqui são de configuração (código), não de usuário: falham o startup. */
    public static FeatureCatalog of(Collection<FeatureDefinition> definitions) {
        Map<FeatureKey, FeatureDefinition> map = new LinkedHashMap<>();
        for (FeatureDefinition def : definitions) {
            if (map.putIfAbsent(def.key(), def) != null) {
                throw new IllegalStateException("Funcionalidade declarada duas vezes: " + def.key());
            }
        }
        for (FeatureKey key : map.keySet()) {
            key.parent().ifPresent(parent -> {
                if (!map.containsKey(parent)) {
                    throw new IllegalStateException(
                            "Funcionalidade '" + key + "' declarada sem o pai '" + parent + "'");
                }
            });
        }
        return new FeatureCatalog(map);
    }

    public Optional<FeatureDefinition> find(FeatureKey key) {
        return Optional.ofNullable(byKey.get(key));
    }

    public boolean contains(FeatureKey key) {
        return byKey.containsKey(key);
    }

    public List<FeatureDefinition> all() {
        return List.copyOf(byKey.values());
    }

    /** Módulos: funcionalidades sem pai. */
    public List<FeatureDefinition> roots() {
        return byKey.values().stream().filter(d -> d.key().parent().isEmpty()).toList();
    }

    public List<FeatureDefinition> children(FeatureKey parent) {
        return byKey.values().stream()
                .filter(d -> d.key().parent().filter(parent::equals).isPresent())
                .toList();
    }

    /** Rejeita permissões que apontam para funcionalidade ou ação que não existe. */
    public void ensureKnown(PermissionSet permissions) {
        permissions.masks().keySet().forEach(this::ensureKnown);
        permissions.actions().forEach((key, names) -> {
            FeatureDefinition def = ensureKnown(key);
            names.forEach(name -> {
                if (!def.extraActions().contains(name)) {
                    throw new UnknownFeatureException("Ação desconhecida '" + name + "' em " + key);
                }
            });
        });
    }

    private FeatureDefinition ensureKnown(FeatureKey key) {
        return find(key).orElseThrow(() -> new UnknownFeatureException("Funcionalidade desconhecida: " + key));
    }
}
