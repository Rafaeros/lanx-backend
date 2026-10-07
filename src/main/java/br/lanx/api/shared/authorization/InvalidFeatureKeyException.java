package br.lanx.api.shared.authorization;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class InvalidFeatureKeyException extends DomainException {
    public InvalidFeatureKeyException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
