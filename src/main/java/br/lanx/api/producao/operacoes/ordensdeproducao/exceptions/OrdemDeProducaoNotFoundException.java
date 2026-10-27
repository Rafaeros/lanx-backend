package br.lanx.api.producao.operacoes.ordensdeproducao.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class OrdemDeProducaoNotFoundException extends DomainException {
    public OrdemDeProducaoNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
