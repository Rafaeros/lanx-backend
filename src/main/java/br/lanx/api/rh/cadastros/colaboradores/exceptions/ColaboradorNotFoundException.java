package br.lanx.api.rh.cadastros.colaboradores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class ColaboradorNotFoundException extends DomainException {
    public ColaboradorNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
