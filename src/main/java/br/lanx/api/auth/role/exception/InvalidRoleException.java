package br.lanx.api.auth.role.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class InvalidRoleException extends DomainException {
    public InvalidRoleException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
