package br.lanx.api.auth.authentication.dto;

import java.time.Instant;

/**
 * Com {@code firstAccess = true} não há token: o usuário ainda está com a senha
 * provisória e precisa defini-la em POST /auth/first-access.
 */
public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, boolean firstAccess) {

    public static LoginResponse bearer(String accessToken, Instant expiresAt) {
        return new LoginResponse(accessToken, "Bearer", expiresAt, false);
    }

    public static LoginResponse firstAccessRequired() {
        return new LoginResponse(null, null, null, true);
    }
}
