package br.lanx.api.auth.user.adapter.out.security;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.lanx.api.auth.user.application.port.out.PasswordHasher;

@Component
class BCryptPasswordHasher implements PasswordHasher {
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
