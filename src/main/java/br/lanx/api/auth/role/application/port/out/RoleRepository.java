package br.lanx.api.auth.role.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.role.domain.Role;

public interface RoleRepository {
    /** Insere se o id é novo, atualiza se já existe. */
    Role save(Role role);

    Optional<Role> findById(UUID id);

    List<Role> findAll();

    List<Role> findByIdIn(Collection<UUID> ids);

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
