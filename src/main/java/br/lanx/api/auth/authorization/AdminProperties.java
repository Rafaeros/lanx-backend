package br.lanx.api.auth.authorization;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credenciais do primeiro administrador. Senha vazia = não cria o usuário. */
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record AdminProperties(String username, String password) {
}
