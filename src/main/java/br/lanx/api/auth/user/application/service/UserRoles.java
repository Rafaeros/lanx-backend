package br.lanx.api.auth.user.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.lanx.api.auth.role.application.port.in.FindRolesUseCase;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.role.domain.exception.RoleNotFoundException;
import br.lanx.api.auth.user.domain.exception.InvalidUserException;

/**
 * O User guarda só ids de role, então o serviço confirma, pelo contrato do
 * módulo role, que todos existem antes de gravar (o banco também protege com
 * chave estrangeira).
 */
final class UserRoles {

    private UserRoles() {
    }

    static Set<UUID> requireExisting(FindRolesUseCase roles, Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new InvalidUserException("Usuário precisa ter pelo menos uma role");
        }
        List<Role> found = roles.findByIds(ids);
        if (found.size() != ids.size()) {
            Set<UUID> missing = new HashSet<>(ids);
            found.forEach(r -> missing.remove(r.id()));
            throw new RoleNotFoundException(missing.stream().map(UUID::toString).collect(Collectors.joining(", ")));
        }
        return Set.copyOf(ids);
    }
}
