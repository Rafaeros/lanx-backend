package br.lanx.api.shared.security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class TokenConfig {

    @Bean
    JwtEncoder jwtEncoder(TokenProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey(props)));
    }

    @Bean
    JwtDecoder jwtDecoder(TokenProperties props) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(signingKey(props))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.issuer()));
        return decoder;
    }

    private static SecretKey signingKey(TokenProperties props) {
        byte[] bytes = Base64.getDecoder().decode(props.secret());
        if (bytes.length < 32) {
            throw new IllegalStateException("app.security.token.secret precisa ter no mínimo 256 bits");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
