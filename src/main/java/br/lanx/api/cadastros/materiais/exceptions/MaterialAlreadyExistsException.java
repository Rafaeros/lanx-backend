package br.lanx.api.cadastros.materiais.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class MaterialAlreadyExistsException extends DomainException {
    public MaterialAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
