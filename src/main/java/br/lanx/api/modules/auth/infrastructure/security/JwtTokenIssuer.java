package br.lanx.api.modules.auth.infrastructure.security;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import br.lanx.api.modules.auth.application.TokenIssuer;
import br.lanx.api.modules.auth.domain.User;
import br.lanx.api.shared.security.TokenClaims;
import br.lanx.api.shared.security.TokenProperties;

@Component
class JwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder encoder;
    private final TokenProperties props;

    JwtTokenIssuer(JwtEncoder encoder, TokenProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public Token issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(props.expiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(TokenClaims.USERNAME, user.username())
                .claim(TokenClaims.PERMISSIONS, List.copyOf(user.authorities()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new Token(value, expiresAt);
    }
}