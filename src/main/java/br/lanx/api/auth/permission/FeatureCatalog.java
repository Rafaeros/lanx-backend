package br.lanx.api.auth.permission;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

/**
 * Catálogo de funcionalidades, montado em memória a partir do que cada módulo
 * declara (FeatureCatalogContributor). A ordem de declaração vira a ordem do
 * menu.
 */
public final class FeatureCatalog {

    private final Map<FeatureKey, FeatureDefinition> byKey;

    private FeatureCatalog(Map<FeatureKey, FeatureDefinition> byKey) {
        this.byKey = byKey;
    }

    /**
     * Erros aqui são de configuração (código), não de usuário: falham o startup.
     */
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

    /**
     * Ações da funcionalidade: o CRUD de sempre + as extras declaradas (em ordem
     * alfabética).
     */
    public static List<String> actionsOf(FeatureDefinition def) {
        Set<String> actions = new LinkedHashSet<>();
        for (Action a : Action.values()) {
            actions.add(a.key());
        }
        def.extraActions().stream().sorted().forEach(actions::add);
        return List.copyOf(actions);
    }

    /** Todos os códigos de permissão que existem: "feature:action". */
    public Set<String> allCodes() {
        Set<String> codes = new LinkedHashSet<>();
        for (FeatureDefinition def : byKey.values()) {
            actionsOf(def).forEach(a -> codes.add(Permission.codeOf(def.key().value(), a)));
        }
        return codes;
    }
}
