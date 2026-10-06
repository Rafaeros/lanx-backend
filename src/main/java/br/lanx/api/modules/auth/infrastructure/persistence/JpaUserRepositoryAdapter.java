package br.lanx.api.modules.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.lanx.api.modules.auth.domain.User;
import br.lanx.api.modules.auth.domain.UserRepository;

@Repository
class JpaUserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository jpa;

    JpaUserRepositoryAdapter(SpringDataUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(UserMapper::toDomain);
    }
}
