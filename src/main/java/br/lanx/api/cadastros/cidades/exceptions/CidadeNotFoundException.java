package br.lanx.api.cadastros.cidades.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class CidadeNotFoundException extends DomainException {
    public CidadeNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
