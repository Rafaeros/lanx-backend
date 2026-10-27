package br.lanx.api.cadastros.clientes.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class ClienteInUseException extends DomainException {
    public ClienteInUseException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
