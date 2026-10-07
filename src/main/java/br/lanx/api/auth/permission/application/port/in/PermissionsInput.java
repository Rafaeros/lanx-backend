package br.lanx.api.auth.permission.application.port.in;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.permission.domain.exception.InvalidPermissionException;
import br.lanx.api.shared.authorization.FeatureKey;

/**
 * Permissões como o cliente as envia: por NOME, não por bitmask.
 * {@code crud}: chave da funcionalidade → ["READ", "UPDATE"].
 * {@code actions}: chave da funcionalidade → ["aprovar"].
 */
public record PermissionsInput(Map<String, Set<String>> crud, Map<String, Set<String>> actions) {

    public PermissionsInput {
        crud = crud == null ? Map.of() : Map.copyOf(crud);
        actions = actions == null ? Map.of() : Map.copyOf(actions);
    }

    public static PermissionsInput empty() {
        return new PermissionsInput(null, null);
    }

    /** Converte nomes em bitmask e valida o formato (o catálogo é validado à parte). */
    public PermissionSet toDomain() {
        Map<FeatureKey, Integer> masks = new HashMap<>();
        crud.forEach((feature, names) -> {
            int mask = 0;
            for (String name : names) {
                mask |= parse(name).bit();
            }
            masks.put(FeatureKey.of(feature), mask);
        });

        Map<FeatureKey, Set<String>> extra = new HashMap<>();
        actions.forEach((feature, names) -> extra.put(FeatureKey.of(feature), new HashSet<>(names)));

        return new PermissionSet(masks, extra);
    }

    private static Action parse(String name) {
        try {
            return Action.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidPermissionException("Ação CRUD inválida: '" + name + "'");
        }
    }
}
