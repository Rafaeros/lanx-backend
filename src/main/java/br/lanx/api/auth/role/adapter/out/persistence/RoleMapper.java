package br.lanx.api.auth.role.adapter.out.persistence;

import br.lanx.api.auth.permission.adapter.out.persistence.PermissionMapper;

import br.lanx.api.auth.role.domain.Role;

final class RoleMapper {

    private RoleMapper() {
    }

    static Role toDomain(RoleEntity e) {
        return new Role(e.getId(), e.getName(), PermissionMapper.toDomain(e.getPermissions(), e.getActions()));
    }

    static RoleEntity toEntity(Role role) {
        return new RoleEntity(
                role.id(),
                role.name(),
                PermissionMapper.toMasks(role.permissions()),
                PermissionMapper.toActions(role.permissions()));
    }
}
