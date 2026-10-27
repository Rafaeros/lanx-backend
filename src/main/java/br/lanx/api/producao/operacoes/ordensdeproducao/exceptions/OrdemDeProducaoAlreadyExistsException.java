package br.lanx.api.producao.operacoes.ordensdeproducao.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class OrdemDeProducaoAlreadyExistsException extends DomainException {
    public OrdemDeProducaoAlreadyExistsException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
