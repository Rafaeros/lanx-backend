package br.lanx.api.modules.auth.fakes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.lanx.api.modules.auth.domain.User;
import br.lanx.api.modules.auth.domain.UserRepository;

/**
 * Implementa o MESMO contrato do JpaUserRepositoryAdapter, mas guarda num Map.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byUsername = new HashMap<>();

    public void save(User user) {
        byUsername.put(user.username(), user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }
}
