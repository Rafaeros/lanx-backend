package br.lanx.api.auth.permission.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.lanx.api.auth.permission.domain.exception.InvalidPermissionException;
import br.lanx.api.shared.authorization.FeatureKey;

/**
 * Conjunto imutável de permissões: máscara CRUD por funcionalidade + ações
 * extras nomeadas. É o mesmo tipo para Role, para as exceções do usuário
 * (grants/denies) e para o resultado final (permissões efetivas).
 */
public record PermissionSet(Map<FeatureKey, Integer> masks, Map<FeatureKey, Set<String>> actions) {

    private static final Pattern ACTION_FORMAT = Pattern.compile("^[a-z][a-z0-9-]{1,49}$");
    public static final PermissionSet EMPTY = new PermissionSet(Map.of(), Map.of());

    public PermissionSet {
        masks = normalizeMasks(masks);
        actions = normalizeActions(actions);
    }

    public static PermissionSet ofMasks(Map<FeatureKey, Integer> masks) {
        return new PermissionSet(masks, Map.of());
    }

    public boolean can(FeatureKey feature, Action action) {
        return action.isIn(masks.getOrDefault(feature, 0));
    }

    public boolean canDo(FeatureKey feature, String action) {
        return actions.getOrDefault(feature, Set.of()).contains(action);
    }

    public boolean isEmpty() {
        return masks.isEmpty() && actions.isEmpty();
    }

    /** Soma: o que qualquer um dos dois permite. */
    public PermissionSet union(PermissionSet other) {
        Map<FeatureKey, Integer> m = new HashMap<>(masks);
        other.masks.forEach((k, v) -> m.merge(k, v, (a, b) -> a | b));
        Map<FeatureKey, Set<String>> a = new HashMap<>();
        Stream.of(actions, other.actions).forEach(src -> src.forEach(
                (k, v) -> a.computeIfAbsent(k, x -> new HashSet<>()).addAll(v)));
        return new PermissionSet(m, a);
    }

    /** Subtração: remove tudo o que {@code other} contém (usado para os denies). */
    public PermissionSet minus(PermissionSet other) {
        Map<FeatureKey, Integer> m = new HashMap<>();
        masks.forEach((k, v) -> m.put(k, v & ~other.masks.getOrDefault(k, 0)));
        Map<FeatureKey, Set<String>> a = new HashMap<>();
        actions.forEach((k, v) -> {
            Set<String> left = new HashSet<>(v);
            left.removeAll(other.actions.getOrDefault(k, Set.of()));
            a.put(k, left);
        });
        return new PermissionSet(m, a);
    }

    /**
     * {compras.analise=5} → "compras.analise:READ", "compras.analise:UPDATE";
     * ação extra → "compras.analise:anexar-arquivo"
     */
    public Set<String> authorities() {
        Stream<String> crud = masks.entrySet().stream()
                .flatMap(e -> Arrays.stream(Action.values())
                        .filter(a -> a.isIn(e.getValue()))
                        .map(a -> e.getKey() + ":" + a.name()));
        Stream<String> extra = actions.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(a -> e.getKey() + ":" + a));
        return Stream.concat(crud, extra).collect(Collectors.toUnmodifiableSet());
    }

    private static Map<FeatureKey, Integer> normalizeMasks(Map<FeatureKey, Integer> in) {
        Map<FeatureKey, Integer> out = new HashMap<>();
        in.forEach((feature, mask) -> {
            if (feature == null || mask == null || mask < 0 || mask > Action.fullMask()) {
                throw new InvalidPermissionException("Máscara inválida para a funcionalidade " + feature);
            }
            if (mask != 0)
                out.put(feature, mask);
        });
        return Map.copyOf(out);
    }

    private static Map<FeatureKey, Set<String>> normalizeActions(Map<FeatureKey, Set<String>> in) {
        Map<FeatureKey, Set<String>> out = new HashMap<>();
        in.forEach((feature, names) -> {
            if (feature == null || names == null) {
                throw new InvalidPermissionException("Ação extra inválida para a funcionalidade " + feature);
            }
            names.forEach(n -> {
                if (n == null || !ACTION_FORMAT.matcher(n).matches()) {
                    throw new InvalidPermissionException("Nome de ação inválido: '" + n + "'");
                }
            });
            if (!names.isEmpty())
                out.put(feature, Set.copyOf(names));
        });
        return Map.copyOf(out);
    }
}
