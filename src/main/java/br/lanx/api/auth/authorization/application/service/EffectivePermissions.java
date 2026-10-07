package br.lanx.api.auth.authorization.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.application.port.out.PermissionCache;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.application.port.in.FindRolesUseCase;
import br.lanx.api.auth.user.application.port.in.FindUserUseCase;
import br.lanx.api.auth.user.domain.User;

/**
 * Resolve as permissões efetivas de um usuário, usando o cache. É aqui que o
 * agregado User (só com ids de role) encontra os agregados Role, cada um pelo
 * contrato do seu módulo.
 */
@Service
class EffectivePermissions {

    private final FindUserUseCase users;
    private final FindRolesUseCase roles;
    private final PermissionCache cache;

    EffectivePermissions(FindUserUseCase users, FindRolesUseCase roles, PermissionCache cache) {
        this.users = users;
        this.roles = roles;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    PermissionSet of(UUID userId) {
        return cache.get(userId).orElseGet(() -> {
            // usuário inexistente ou inativo não tem nenhuma permissão
            PermissionSet permissions = users.findById(userId)
                    .filter(User::active)
                    .map(user -> user.effectivePermissions(roles.findByIds(user.roleIds())))
                    .orElse(PermissionSet.EMPTY);
            cache.put(userId, permissions);
            return permissions;
        });
    }
}
