package br.lanx.api.cadastros.clientes.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class ClienteNotFoundException extends DomainException {
    public ClienteNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
