package br.lanx.api.compras.relatorios.iqf.domain;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class PeriodoIqfInvalidoException extends DomainException {

    public PeriodoIqfInvalidoException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
