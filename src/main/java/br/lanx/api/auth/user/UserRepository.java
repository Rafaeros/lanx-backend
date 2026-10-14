package br.lanx.api.auth.user;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * Códigos de permissão do usuário ATIVO, em uma consulta só (usuário → roles →
     * permissões). Usuário inativo ou removido não tem nenhuma.
     */
    @Query("""
            select distinct p.code
            from User u join u.roles r join r.permissions p
            where u.id = :userId and u.active = true
            """)
    Set<String> findAuthorityCodes(@Param("userId") UUID userId);
}
