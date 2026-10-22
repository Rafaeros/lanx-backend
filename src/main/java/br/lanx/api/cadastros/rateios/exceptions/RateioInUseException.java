package br.lanx.api.cadastros.rateios.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class RateioInUseException extends DomainException {
    public RateioInUseException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
