package br.lanx.api.rh.cadastros.colaboradores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class ColaboradorAlreadyExistsException extends DomainException {
    public ColaboradorAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
