package br.lanx.api.rh.cadastros.colaboradores.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class PrincipalDuplicadoException extends DomainException {
    public PrincipalDuplicadoException(String campo) {
        super(ErrorType.VALIDATION, "Só é permitido um " + campo + " principal");
    }
}
