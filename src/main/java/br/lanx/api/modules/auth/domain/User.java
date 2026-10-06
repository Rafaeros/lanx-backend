package br.lanx.api.modules.auth.domain;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.lanx.api.modules.auth.domain.exceptions.UserInactiveException;

public record User(UUID id, String username, String passwordHash, boolean active, Set<Role> roles) {

    public User {
        roles = Set.copyOf(roles);
    }

    public void ensureCanLogin() {
        if (!active)
            throw new UserInactiveException();
    }

    public Set<String> authorities() {
        return roles.stream()
                .flatMap(r -> r.authorities().stream())
                .collect(Collectors.toUnmodifiableSet());
    }
}
