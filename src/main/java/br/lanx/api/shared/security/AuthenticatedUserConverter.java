package br.lanx.api.shared.security;

import java.util.List;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Por padrão o Spring colocaria o objeto Jwt como "principal".
 * Trocamos pelo nosso AuthenticatedUser: assim nenhum módulo precisa conhecer
 * JWT. Sem authorities: a autorização por funcionalidade é feita pelo módulo
 * auth (PermissionInterceptor), não pelo Spring Security.
 */
class AuthenticatedUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var user = new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString(TokenClaims.USERNAME));

        // construtor com 3 argumentos = já autenticado
        return new UsernamePasswordAuthenticationToken(user, jwt, List.of());
    }
}
