package br.lanx.api.auth.role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Override
    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = "permissions")
    List<Role> findAll();

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByName(String name);

    List<Role> findByIdIn(Collection<UUID> ids);

    boolean existsByName(String name);
}
