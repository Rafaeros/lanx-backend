package br.lanx.api.modules.auth.application;

import java.time.Instant;

public record LoginResult(String accessToken, Instant expiresAt) {

}
