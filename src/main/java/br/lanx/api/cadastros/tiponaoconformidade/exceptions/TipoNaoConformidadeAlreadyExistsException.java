package br.lanx.api.cadastros.tiponaoconformidade.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class TipoNaoConformidadeAlreadyExistsException extends DomainException {
    public TipoNaoConformidadeAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
