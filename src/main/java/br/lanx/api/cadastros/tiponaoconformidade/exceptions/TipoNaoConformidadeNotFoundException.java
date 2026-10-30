package br.lanx.api.cadastros.tiponaoconformidade.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class TipoNaoConformidadeNotFoundException extends DomainException {
    public TipoNaoConformidadeNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
