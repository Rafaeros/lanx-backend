package br.lanx.api.auth.user.adapter.in.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credenciais do primeiro administrador. Senha vazia = não cria nada. */
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record AdminProperties(String username, String password) {
}
