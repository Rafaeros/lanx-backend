package br.lanx.api.auth.permission.adapter.out.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.shared.authorization.FeatureKey;

/** PermissionSet (domínio) ↔ colunas (máscaras e ações extras) das tabelas. */
public final class PermissionMapper {

    private PermissionMapper() {
    }

    public static PermissionSet toDomain(Map<String, Integer> masks, Set<FeatureActionEmbeddable> actions) {
        Map<FeatureKey, Integer> m = new HashMap<>();
        masks.forEach((feature, mask) -> m.put(FeatureKey.of(feature), mask));

        Map<FeatureKey, Set<String>> a = new HashMap<>();
        actions.forEach(fa -> a.computeIfAbsent(FeatureKey.of(fa.getFeature()), k -> new HashSet<>())
                .add(fa.getAction()));

        return new PermissionSet(m, a);
    }

    public static Map<String, Integer> toMasks(PermissionSet set) {
        Map<String, Integer> out = new HashMap<>();
        set.masks().forEach((feature, mask) -> out.put(feature.value(), mask));
        return out;
    }

    public static Set<FeatureActionEmbeddable> toActions(PermissionSet set) {
        Set<FeatureActionEmbeddable> out = new HashSet<>();
        set.actions().forEach((feature, names) -> names
                .forEach(name -> out.add(new FeatureActionEmbeddable(feature.value(), name))));
        return out;
    }
}
