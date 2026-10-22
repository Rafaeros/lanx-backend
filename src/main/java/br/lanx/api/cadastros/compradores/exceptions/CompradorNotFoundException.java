package br.lanx.api.cadastros.compradores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class CompradorNotFoundException extends DomainException {
    public CompradorNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
