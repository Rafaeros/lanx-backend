package br.lanx.api.auth.role.adapter.out.persistence;

import br.lanx.api.auth.permission.adapter.out.persistence.PermissionMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.role.domain.Role;

@Repository
class RolePersistenceAdapter implements RoleRepository {
    private final SpringDataRoleRepository jpa;

    RolePersistenceAdapter(SpringDataRoleRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = jpa.findById(role.id())
                .map(existing -> {
                    existing.update(role.name(),
                            PermissionMapper.toMasks(role.permissions()),
                            PermissionMapper.toActions(role.permissions()));
                    return existing;
                })
                .orElseGet(() -> RoleMapper.toEntity(role));
        return RoleMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpa.findById(id).map(RoleMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll().stream().map(RoleMapper::toDomain).toList();
    }

    @Override
    public List<Role> findByIdIn(Collection<UUID> ids) {
        return jpa.findByIdIn(ids).stream().map(RoleMapper::toDomain).toList();
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpa.findByName(name).map(RoleMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsByName(name);
    }
}
