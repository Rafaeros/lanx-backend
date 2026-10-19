package br.lanx.api.auth.user;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import br.lanx.api.auth.user.exception.InvalidUserException;

/**
 * Regra de senha em texto puro. O User só guarda o hash, então esta validação
 * roda no service, ANTES de hashear.
 */
final class PasswordPolicy {

    static final int MIN_LENGTH = 8;
    static final int MAX_LENGTH = 150;

    /** Senha definida pelo próprio usuário no primeiro acesso. */
    static final int STRONG_MIN_LENGTH = 10;

    /** O BCrypt só considera os primeiros 72 bytes (e o Spring recusa acima disso). */
    static final int BCRYPT_MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    /** Senha provisória (cadastro pelo administrador, bootstrap): letras e números. */
    static void validate(String raw) {
        if (raw == null || raw.length() < MIN_LENGTH || raw.length() > MAX_LENGTH) {
            throw new InvalidUserException(
                    "A senha deve ter entre " + MIN_LENGTH + " e " + MAX_LENGTH + " caracteres");
        }
        boolean hasLetter = raw.chars().anyMatch(Character::isLetter);
        boolean hasDigit = raw.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new InvalidUserException("A senha deve conter letras e números");
        }
    }

    /**
     * Senha forte, escolhida pelo usuário: 10+ caracteres, maiúscula, minúscula,
     * número e símbolo, sem espaços e sem conter o próprio username.
     */
    static void validateStrong(String raw, String username) {
        if (raw == null || raw.length() < STRONG_MIN_LENGTH) {
            throw new InvalidUserException("A senha deve ter pelo menos " + STRONG_MIN_LENGTH + " caracteres");
        }
        if (raw.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidUserException("A senha é longa demais (máximo de " + BCRYPT_MAX_BYTES + " caracteres)");
        }
        if (raw.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidUserException("A senha não pode ter espaços");
        }
        if (raw.chars().noneMatch(Character::isUpperCase)) {
            throw new InvalidUserException("A senha precisa de pelo menos uma letra maiúscula");
        }
        if (raw.chars().noneMatch(Character::isLowerCase)) {
            throw new InvalidUserException("A senha precisa de pelo menos uma letra minúscula");
        }
        if (raw.chars().noneMatch(Character::isDigit)) {
            throw new InvalidUserException("A senha precisa de pelo menos um número");
        }
        if (raw.chars().allMatch(Character::isLetterOrDigit)) {
            throw new InvalidUserException("A senha precisa de pelo menos um símbolo (ex.: ! @ # $ %)");
        }
        if (username != null && !username.isBlank()
                && raw.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
            throw new InvalidUserException("A senha não pode conter o nome de usuário");
        }
    }
}
