package br.lanx.api.modules.auth.domain;

import java.util.Optional;

/** O domínio diz O QUE precisa. COMO (JPA, Mongo, API) é problema da infraestrutura. */
public interface UserRepository {
    Optional<User> findByUsername(String username);
}