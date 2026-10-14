package br.lanx.api.auth.authentication.dto;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt) {

    public static LoginResponse bearer(String accessToken, Instant expiresAt) {
        return new LoginResponse(accessToken, "Bearer", expiresAt);
    }
}
