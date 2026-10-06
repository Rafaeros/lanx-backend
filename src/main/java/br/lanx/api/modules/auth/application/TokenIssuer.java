package br.lanx.api.modules.auth.application;

import java.time.Instant;

import br.lanx.api.modules.auth.domain.User;

public interface TokenIssuer {
    Token issue(User user);

    record Token(String value, Instant expiresAt) {
    }
}