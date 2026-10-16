package br.lanx.api.compras.relatorios.analisepedidos.domain;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class FiltroInvalidoException extends DomainException {

    public FiltroInvalidoException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
