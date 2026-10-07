package br.lanx.api.auth.fakes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.role.domain.Role;

public class InMemoryRoleRepository implements RoleRepository {

    private final Map<UUID, Role> byId = new HashMap<>();

    @Override
    public Role save(Role role) {
        byId.put(role.id(), role);
        return role;
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Role> findAll() {
        return List.copyOf(byId.values());
    }

    @Override
    public List<Role> findByIdIn(Collection<UUID> ids) {
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    @Override
    public Optional<Role> findByName(String name) {
        return byId.values().stream().filter(r -> r.name().equals(name)).findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }
}
