package br.lanx.api.cadastros.materiais.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class MaterialUnidadeMedidaNaoEncontradaException extends DomainException {

    public MaterialUnidadeMedidaNaoEncontradaException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}