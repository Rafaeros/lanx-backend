package br.lanx.api.auth.fakes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.user.domain.User;

/**
 * Implementa o MESMO contrato do UserPersistenceAdapter, mas guarda num Map.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byUsername = new HashMap<>();

    @Override
    public User save(User user) {
        byUsername.put(user.username(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return byUsername.values().stream().filter(u -> u.id().equals(id)).findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return byUsername.containsKey(username);
    }
}
