package br.lanx.api.modules.auth.infrastructure.persistence;

import java.util.stream.Collectors;

import br.lanx.api.modules.auth.domain.Role;
import br.lanx.api.modules.auth.domain.User;

final class UserMapper {

    private UserMapper() {
    }

    static User toDomain(UserEntity e) {
        return new User(
                e.getId(),
                e.getUsername(),
                e.getPasswordHash(),
                e.isActive(),
                e.getRoles().stream().map(UserMapper::toDomain).collect(Collectors.toSet()));
    }

    static Role toDomain(RoleEntity e) {
        return new Role(e.getId(), e.getName(), e.getPermissions());
    }
}
