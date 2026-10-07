package br.lanx.api.auth.user.adapter.out.persistence;

import br.lanx.api.auth.permission.adapter.out.persistence.PermissionMapper;
import br.lanx.api.auth.user.domain.User;

final class UserMapper {

    private UserMapper() {
    }

    // ---------- banco → domínio (leitura) ----------

    static User toDomain(UserEntity e) {
        return new User(
                e.getId(),
                e.getUsername(),
                e.getPasswordHash(),
                e.isActive(),
                e.getRoleIds(),
                PermissionMapper.toDomain(e.getGrantMasks(), e.getGrantActions()),
                PermissionMapper.toDomain(e.getDenyMasks(), e.getDenyActions()));
    }

    // ---------- domínio → banco (escrita) ----------

    static UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.username(),
                user.passwordHash(),
                user.active(),
                user.roleIds(),
                PermissionMapper.toMasks(user.grants()),
                PermissionMapper.toActions(user.grants()),
                PermissionMapper.toMasks(user.denies()),
                PermissionMapper.toActions(user.denies()));
    }
}
