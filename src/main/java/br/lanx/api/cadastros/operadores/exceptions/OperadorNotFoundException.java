package br.lanx.api.cadastros.operadores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class OperadorNotFoundException extends DomainException {
    public OperadorNotFoundException(String mesage) {
        super(ErrorType.NOT_FOUND, mesage);
    }
}
