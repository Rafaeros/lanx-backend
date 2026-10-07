package br.lanx.api.auth.authentication.application.port.in;

public record LoginCommand(String username, String password) {
}
