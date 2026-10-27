package br.lanx.api.comercial.relatorios.carteira.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class CarteiraErpIndisponivelException extends DomainException {
    public CarteiraErpIndisponivelException(String message) {
        super(ErrorType.UNAVAILABLE, message);
    }
}
