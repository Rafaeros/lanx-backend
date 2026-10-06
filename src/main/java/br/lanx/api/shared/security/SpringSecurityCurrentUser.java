package br.lanx.api.shared.security;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class SpringSecurityCurrentUser implements CurrentUser {
    @Override
    public UUID id() {
        return get().id();
    }

    @Override
    public String username() {
        return get().username();
    }

    @Override
    public boolean hasPermission(String p) {
        return get().hasPermission(p);
    }

    private AuthenticatedUser get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new AuthenticationCredentialsNotFoundException("Nenhum usuário autenticado");
    }
}
