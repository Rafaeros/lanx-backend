package br.lanx.api.comercial.relatorios.carteira.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class PeriodoCarteiraInvalidoException extends DomainException {
    public PeriodoCarteiraInvalidoException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
