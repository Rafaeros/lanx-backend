package br.lanx.api.cadastros.pais.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class PaisNotFoundException extends DomainException {
    public PaisNotFoundException(String identificador) {
        super(ErrorType.NOT_FOUND, "País não encontrado: " + identificador);
    }
}
