package br.lanx.api.cadastros.fornecedores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class FornecedorAlreadyExistsException extends DomainException {
    public FornecedorAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
