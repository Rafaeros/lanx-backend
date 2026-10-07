package br.lanx.api.auth.role.domain;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.PermissionSet;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import br.lanx.api.auth.role.domain.exception.InvalidRoleException;
import br.lanx.api.shared.authorization.FeatureKey;
import br.lanx.api.shared.domain.Ids;

public record Role(UUID id, String name, PermissionSet permissions) {

    private static final Pattern NAME_FORMAT = Pattern.compile("^[A-Z0-9_]{3,50}$");

    /** Construtor canônico: usado também para reidratar do banco (sem revalidar). */
    public Role {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(permissions, "permissions");
    }

    /** Fábrica: único caminho para criar uma role NOVA, com todas as regras. */
    public static Role create(String name, PermissionSet permissions) {
        return new Role(Ids.newId(), validatedName(name, permissions), permissions);
    }

    /** Devolve a role alterada (mesmo id), com as mesmas regras da criação. */
    public Role update(String newName, PermissionSet newPermissions) {
        return new Role(id, validatedName(newName, newPermissions), newPermissions);
    }

    public boolean allows(FeatureKey feature, Action action) {
        return permissions.can(feature, action);
    }

    public Set<String> authorities() {
        return permissions.authorities();
    }

    /** " comprador " → "COMPRADOR" */
    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
    }

    private static String validatedName(String name, PermissionSet permissions) {
        String normalized = normalizeName(name);
        if (!NAME_FORMAT.matcher(normalized).matches()) {
            throw new InvalidRoleException("O nome da role deve ter de 3 a 50 caracteres: letras, números ou _");
        }
        if (permissions == null || permissions.isEmpty()) {
            throw new InvalidRoleException("Role precisa ter pelo menos uma permissão");
        }
        return normalized;
    }
}
