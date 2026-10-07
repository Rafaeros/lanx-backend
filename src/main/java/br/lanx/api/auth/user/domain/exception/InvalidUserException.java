package br.lanx.api.auth.user.domain.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class InvalidUserException extends DomainException {
    public InvalidUserException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
