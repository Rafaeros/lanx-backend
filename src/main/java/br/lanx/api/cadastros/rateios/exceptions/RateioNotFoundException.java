package br.lanx.api.cadastros.rateios.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class RateioNotFoundException extends DomainException {
    public RateioNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
