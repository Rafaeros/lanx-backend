package br.lanx.api.auth.permission.domain.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UnknownFeatureException extends DomainException {
    public UnknownFeatureException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
