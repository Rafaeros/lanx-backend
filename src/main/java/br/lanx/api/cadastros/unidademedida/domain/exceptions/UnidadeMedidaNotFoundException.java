package br.lanx.api.cadastros.unidademedida.domain.exceptions;

import java.util.UUID;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UnidadeMedidaNotFoundException extends DomainException {
    public UnidadeMedidaNotFoundException(UUID id) {
        super(ErrorType.NOT_FOUND, "Unidade de medida não encontrada: " + id);
    }
}
