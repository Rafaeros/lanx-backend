package br.lanx.api.cadastros.enderecos.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class CepInvalidoException extends DomainException {
    public CepInvalidoException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
