package br.lanx.api.cadastros.operadores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class OperadorAlreadyExistsException extends DomainException {
    public OperadorAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
