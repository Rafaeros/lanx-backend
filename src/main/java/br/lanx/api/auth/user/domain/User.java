package br.lanx.api.auth.user.domain;

import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.domain.Role;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import br.lanx.api.auth.user.domain.exception.InvalidUserException;
import br.lanx.api.auth.user.domain.exception.UserInactiveException;
import br.lanx.api.shared.domain.Ids;

/**
 * Agregado User. Referencia as roles só pelo id: Role é outro agregado, com o
 * próprio ciclo de vida. Quem precisa das permissões efetivas carrega as roles
 * e chama {@link #effectivePermissions(Collection)}.
 */
public record User(UUID id, String username, String passwordHash, boolean active, Set<UUID> roleIds,
        PermissionSet grants, PermissionSet denies) {

    private static final Pattern USERNAME_FORMAT = Pattern.compile("^[a-z0-9._-]{3,50}$");

    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(grants, "grants");
        Objects.requireNonNull(denies, "denies");
        roleIds = Set.copyOf(roleIds);
    }

    /** Usuário sem exceções individuais: só vale o que as roles dão. */
    public User(UUID id, String username, String passwordHash, boolean active, Set<UUID> roleIds) {
        this(id, username, passwordHash, active, roleIds, PermissionSet.EMPTY, PermissionSet.EMPTY);
    }

    public static User create(String username, String passwordHash, Set<UUID> roleIds) {
        return create(username, passwordHash, roleIds, PermissionSet.EMPTY, PermissionSet.EMPTY);
    }

    public static User create(String username, String passwordHash, Set<UUID> roleIds,
            PermissionSet grants, PermissionSet denies) {
        String normalized = normalizeUsername(username);
        if (!USERNAME_FORMAT.matcher(normalized).matches()) {
            throw new InvalidUserException("O Username deve ter entre 3 a 50 caracteres, com letras ou números");
        }
        requireRoles(roleIds);

        return new User(Ids.newId(), normalized, passwordHash, true, roleIds, grants, denies);
    }

    /** Altera roles, exceções e situação. Username e senha não mudam por aqui. */
    public User update(Set<UUID> newRoleIds, PermissionSet newGrants, PermissionSet newDenies, boolean newActive) {
        requireRoles(newRoleIds);
        return new User(id, username, passwordHash, newActive, newRoleIds, newGrants, newDenies);
    }

    public void ensureCanLogin() {
        if (!active)
            throw new UserInactiveException();
    }

    /**
     * Permissão efetiva = união das roles do usuário + grants − denies (deny sempre
     * vence). Roles que não pertencem a este usuário são ignoradas.
     */
    public PermissionSet effectivePermissions(Collection<Role> loadedRoles) {
        PermissionSet fromRoles = loadedRoles.stream()
                .filter(role -> roleIds.contains(role.id()))
                .map(r -> r.permissions())
                .reduce(PermissionSet.EMPTY, (acc, permissions) -> acc.union(permissions));
        return fromRoles.union(grants).minus(denies);
    }

    private static void requireRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new InvalidUserException("Usuário precisa ter pelo menos uma role");
        }
    }

    /**
     * " Joao.Silva " → "joao.silva". Usado também no login, para "Joao" e "joao"
     * serem o mesmo usuário.
     */
    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
