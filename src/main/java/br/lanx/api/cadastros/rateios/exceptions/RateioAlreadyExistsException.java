package br.lanx.api.cadastros.rateios.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class RateioAlreadyExistsException extends DomainException {
    public RateioAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
