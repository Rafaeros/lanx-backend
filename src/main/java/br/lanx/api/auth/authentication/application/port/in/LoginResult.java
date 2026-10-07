package br.lanx.api.auth.authentication.application.port.in;

import java.time.Instant;

public record LoginResult(String accessToken, Instant expiresAt) {
}
