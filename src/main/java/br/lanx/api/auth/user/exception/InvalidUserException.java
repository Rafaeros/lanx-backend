package br.lanx.api.auth.user.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class InvalidUserException extends DomainException {
    public InvalidUserException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
