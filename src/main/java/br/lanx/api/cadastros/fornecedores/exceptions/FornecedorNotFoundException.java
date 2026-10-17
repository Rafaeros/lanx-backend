package br.lanx.api.cadastros.fornecedores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class FornecedorNotFoundException extends DomainException {
    public FornecedorNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
