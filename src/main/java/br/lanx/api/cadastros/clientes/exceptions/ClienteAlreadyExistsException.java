package br.lanx.api.cadastros.clientes.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class ClienteAlreadyExistsException extends DomainException {
    public ClienteAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
