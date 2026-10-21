package br.lanx.api.cadastros.materiais.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class MaterialUnidadeMedidaNotFoundException extends DomainException {

    public MaterialUnidadeMedidaNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}