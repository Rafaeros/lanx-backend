package br.lanx.api.modules.auth.fakes;

import br.lanx.api.modules.auth.application.PasswordHasher;

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