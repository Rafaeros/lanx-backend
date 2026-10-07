package br.lanx.api.auth.authentication.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** O módulo authentication só precisa de quem é o usuário (id e username) para emitir o token. */
public interface TokenIssuer {
    Token issue(UUID userId, String username);

    record Token(String value, Instant expiresAt) {
    }
}
