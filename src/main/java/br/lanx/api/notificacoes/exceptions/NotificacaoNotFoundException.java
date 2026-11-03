package br.lanx.api.notificacoes.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class NotificacaoNotFoundException extends DomainException {
    public NotificacaoNotFoundException(String message) {
        super(ErrorType.NOT_FOUND, message);
    }
}
