package br.lanx.api.testeinspecao.operacoes.naoconformidade.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class AnexoInvalidoException extends DomainException {
    public AnexoInvalidoException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
