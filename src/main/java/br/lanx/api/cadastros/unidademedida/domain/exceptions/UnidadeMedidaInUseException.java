package br.lanx.api.cadastros.unidademedida.domain.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UnidadeMedidaInUseException extends DomainException {
    public UnidadeMedidaInUseException() {
        super(ErrorType.CONFLICT, "Unidade de medida em uso por outros cadastros e não pode ser excluída");
    }
}
