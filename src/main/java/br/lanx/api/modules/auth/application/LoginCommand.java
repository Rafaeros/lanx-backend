package br.lanx.api.modules.auth.application;

public record LoginCommand(String username, String password) {
}
