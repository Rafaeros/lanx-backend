package br.lanx.api.auth.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        // HashMap: quem consulta pode perguntar por id nulo sem estourar NPE
        Map<UUID, String> usernames = new HashMap<>();
        if (!userIds.isEmpty()) {
            users.findAllById(userIds).forEach(user -> usernames.put(user.getId(), user.getUsername()));
        }
        return usernames;
    }
}
