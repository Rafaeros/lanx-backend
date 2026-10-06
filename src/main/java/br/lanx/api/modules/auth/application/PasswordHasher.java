package br.lanx.api.modules.auth.application;

public interface PasswordHasher {
    boolean matches(String rawPassword, String hash);

    String hash(String rawPassword);
}