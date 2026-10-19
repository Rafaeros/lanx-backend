package br.lanx.api.auth.user.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class FirstAccessAlreadyCompletedException extends DomainException {
    public FirstAccessAlreadyCompletedException() {
        super(ErrorType.CONFLICT, "O primeiro acesso já foi concluído. Entre com a sua senha.");
    }
}
