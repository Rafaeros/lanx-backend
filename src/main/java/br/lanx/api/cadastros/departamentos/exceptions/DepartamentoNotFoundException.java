package br.lanx.api.cadastros.departamentos.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class DepartamentoNotFoundException extends DomainException {
    public DepartamentoNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
