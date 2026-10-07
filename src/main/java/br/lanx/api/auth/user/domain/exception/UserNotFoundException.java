package br.lanx.api.auth.user.domain.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String reference) {
        super(ErrorType.NOT_FOUND, "Usuário não encontrado: " + reference);
    }
}
