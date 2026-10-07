package br.lanx.api.auth.user.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.lanx.api.auth.permission.adapter.out.persistence.PermissionMapper;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.user.domain.User;

@Repository
class UserPersistenceAdapter implements UserRepository {
    private final SpringDataUserRepository jpa;

    UserPersistenceAdapter(SpringDataUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        UserEntity entity = jpa.findFullById(user.id())
                .map(existing -> {
                    existing.update(user.active(), user.roleIds(),
                            PermissionMapper.toMasks(user.grants()), PermissionMapper.toActions(user.grants()),
                            PermissionMapper.toMasks(user.denies()), PermissionMapper.toActions(user.denies()));
                    return existing;
                })
                .orElseGet(() -> UserMapper.toEntity(user));
        return UserMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findFullById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }
}
