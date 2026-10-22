package br.lanx.api.auth.user;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.compradores.UsuarioLookup;

/** Usuários do LANX para o vínculo dos compradores (cadastros não conhece auth). */
@Component
class UsuarioLookupAdapter implements UsuarioLookup {

    private final UserRepository users;

    UsuarioLookupAdapter(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean exists(UUID userId) {
        return users.existsById(userId);
    }

    @Override
    public Map<UUID, String> usernames(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return users.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, User::getUsername));
    }
}
