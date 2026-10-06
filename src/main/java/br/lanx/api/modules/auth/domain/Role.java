package br.lanx.api.modules.auth.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record Role(UUID id, String name, Map<Feature, Integer> masks) {

    public Role {
        masks = Map.copyOf(masks);
    }

    /** {COMPRAS=5} → {"COMPRAS:READ", "COMPRAS:UPDATE"} */
    public Set<String> authorities() {
        return masks.entrySet().stream()
                .flatMap(e -> Arrays.stream(Action.values())
                        .filter(a -> a.isIn(e.getValue()))
                        .map(a -> e.getKey().name() + ":" + a.name()))
                .collect(Collectors.toUnmodifiableSet());
    }
}