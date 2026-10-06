package br.lanx.api.modules.auth.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Package-private: só o adaptador deste pacote enxerga o Spring Data. */
interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    // carrega user + roles + permissões numa consulta só (evita N+1 e
    // LazyInitializationException)
    @EntityGraph(attributePaths = { "roles", "roles.permissions" })
    Optional<UserEntity> findByUsername(String username);
}