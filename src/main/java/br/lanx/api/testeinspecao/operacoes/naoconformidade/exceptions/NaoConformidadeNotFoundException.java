package br.lanx.api.testeinspecao.operacoes.naoconformidade.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class NaoConformidadeNotFoundException extends DomainException {
    public NaoConformidadeNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
