package br.lanx.api.auth.fakes;

import br.lanx.api.auth.user.application.port.out.PasswordHasher;

/** "Hash" previsível: senha123 → hashed:senha123. Rápido e legível no teste. */
public class FakePasswordHasher implements PasswordHasher {

    @Override
    public boolean matches(String rawPassword, String hash) {
        return hash.equals(hash(rawPassword));
    }

    @Override
    public String hash(String rawPassword) {
        return "hashed:" + rawPassword;
    }
}