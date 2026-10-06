package br.lanx.api.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.token")
public record TokenProperties(String secret, String issuer, Duration expiration) {
}