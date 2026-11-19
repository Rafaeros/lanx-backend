package br.lanx.api.cadastros.estados.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class EstadoNotFoundException extends DomainException {
    public EstadoNotFoundException(String identificador) {
        super(ErrorType.NOT_FOUND, "Estado não encontrado: " + identificador);
    }
}
