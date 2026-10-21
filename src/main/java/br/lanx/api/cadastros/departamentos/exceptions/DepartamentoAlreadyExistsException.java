package br.lanx.api.cadastros.departamentos.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class DepartamentoAlreadyExistsException extends DomainException {
    public DepartamentoAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
