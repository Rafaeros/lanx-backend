package br.lanx.api.modules.auth.infrastructure.security;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.lanx.api.modules.auth.application.PasswordHasher;

@Component
class SpringPasswordHasher implements PasswordHasher {
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

}
