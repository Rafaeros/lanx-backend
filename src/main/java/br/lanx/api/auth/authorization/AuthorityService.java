package br.lanx.api.auth.authorization;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.user.UserRepository;
import br.lanx.api.shared.security.AuthorityResolver;

/**
 * Resolve as authorities do usuário do token (com cache). É chamado pelo
 * conversor de JWT a cada requisição autenticada. Usuário inativo ou removido
 * volta vazio: autenticado, mas sem nenhuma permissão.
 */
@Service
class AuthorityService implements AuthorityResolver {

    private final UserRepository users;
    private final AuthorityCache cache;

    AuthorityService(UserRepository users, AuthorityCache cache) {
        this.users = users;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> authoritiesOf(UUID userId) {
        return cache.get(userId).orElseGet(() -> {
            Set<String> authorities = users.findAuthorityCodes(userId);
            cache.put(userId, authorities);
            return authorities;
        });
    }
}
