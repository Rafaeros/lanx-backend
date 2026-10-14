package br.lanx.api.auth.permission.application.port.in;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.PermissionSet;

/**
 * Permissões como o cliente as recebe, no mesmo formato de {@link PermissionsInput}:
 * pode ser devolvido direto para pré-marcar os checkboxes.
 */
public record PermissionsView(Map<String, List<String>> crud, Map<String, List<String>> actions) {

    public static PermissionsView from(PermissionSet set) {
        Map<String, List<String>> crud = new TreeMap<>();
        set.masks().forEach((feature, mask) -> crud.put(feature.value(), Arrays.stream(Action.values())
                .filter(a -> a.isIn(mask))
                .map(a -> a.name())
                .toList()));

        Map<String, List<String>> actions = new TreeMap<>();
        set.actions().forEach((feature, names) -> actions.put(feature.value(), names.stream().sorted().toList()));

        return new PermissionsView(crud, actions);
    }
}
