package br.lanx.api.cadastros.unidade_medida.domain.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UnidadeMedidaAlreadyExistsException extends DomainException {
    public UnidadeMedidaAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
