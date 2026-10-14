package br.lanx.api.shared.security;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Transforma o JWT já validado em Authentication. O token carrega só a
 * identidade; as authorities vêm do servidor (com cache), então mudar uma role
 * vale na hora, sem esperar o token expirar. O principal é o
 * {@link AuthenticatedUser}: nenhum módulo precisa conhecer JWT.
 */
@Component
class JwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AuthorityResolver resolver;

    JwtAuthoritiesConverter(AuthorityResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var user = new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString(TokenClaims.USERNAME));

        var authorities = resolver.authoritiesOf(user.id()).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());

        // construtor com 3 argumentos = já autenticado
        return new UsernamePasswordAuthenticationToken(user, jwt, authorities);
    }
}
