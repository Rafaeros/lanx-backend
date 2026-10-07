package br.lanx.api.auth.role.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataRoleRepository extends JpaRepository<RoleEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = { "permissions", "actions" })
    Optional<RoleEntity> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = { "permissions", "actions" })
    List<RoleEntity> findAll();

    @EntityGraph(attributePaths = { "permissions", "actions" })
    List<RoleEntity> findByIdIn(Collection<UUID> ids);

    @EntityGraph(attributePaths = { "permissions", "actions" })
    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);
}
