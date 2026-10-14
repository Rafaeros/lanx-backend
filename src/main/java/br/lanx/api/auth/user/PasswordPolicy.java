package br.lanx.api.auth.user;

import br.lanx.api.auth.user.exception.InvalidUserException;

/**
 * Regra de senha em texto puro. O User só guarda o hash, então esta validação
 * roda no service, ANTES de hashear.
 */
final class PasswordPolicy {

    static final int MIN_LENGTH = 8;
    static final int MAX_LENGTH = 150;

    private PasswordPolicy() {
    }

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
}
