package br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class DesenhoNaoPodeSerDeletadoException extends DomainException {
    public DesenhoNaoPodeSerDeletadoException(String message) {
        super(ErrorType.BUSINESS_RULE, message);
    }
}
