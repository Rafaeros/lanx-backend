package br.lanx.api.cadastros.compradores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class CompradorAlreadyExistsException extends DomainException {
    public CompradorAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
