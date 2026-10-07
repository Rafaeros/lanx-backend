package br.lanx.api.auth.user.application.port.out;

public interface PasswordHasher {
    boolean matches(String rawPassword, String hash);

    String hash(String rawPassword);
}