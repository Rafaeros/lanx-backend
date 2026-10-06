package br.lanx.api.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Por padrão o Spring colocaria o objeto Jwt como "principal".
 * Trocamos pelo nosso AuthenticatedUser: assim nenhum módulo precisa conhecer
 * JWT.
 */
class AuthenticatedUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> perms = Optional
                .ofNullable(jwt.getClaimAsStringList(TokenClaims.PERMISSIONS))
                .orElse(List.of());

        var user = new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString(TokenClaims.USERNAME),
                Set.copyOf(perms));

        // authorities = o que o @PreAuthorize("hasAuthority(...)") enxerga
        var authorities = perms.stream().map(SimpleGrantedAuthority::new).toList();

        // construtor com 3 argumentos = já autenticado
        return new UsernamePasswordAuthenticationToken(user, jwt, authorities);
    }
}