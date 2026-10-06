package br.lanx.api.shared.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String username,
        Set<String> permissions) {

    public AuthenticatedUser {
        permissions = Set.copyOf(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

}
