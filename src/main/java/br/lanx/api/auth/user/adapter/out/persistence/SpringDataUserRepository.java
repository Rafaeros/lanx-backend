package br.lanx.api.auth.user.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Package-private: só o adaptador deste pacote enxerga o Spring Data. */
interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    // O User guarda só os ids das roles e as próprias exceções; as permissões
    // das roles não são carregadas aqui (vêm do RoleRepository, por id).
    @EntityGraph(attributePaths = { "roleIds", "grantMasks", "denyMasks", "grantActions", "denyActions" })
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = { "roleIds", "grantMasks", "denyMasks", "grantActions", "denyActions" })
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findFullById(UUID id);

    boolean existsByUsername(String username);
}
