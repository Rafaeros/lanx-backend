package br.lanx.api.auth.user.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.user.domain.User;

/**
 * O domínio diz O QUE precisa. COMO (JPA, Mongo, API) é problema da
 * infraestrutura.
 */
public interface UserRepository {
    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
