package br.lanx.api.modules.auth.domain.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UserInactiveException extends DomainException {
    public UserInactiveException() {
        super(ErrorType.FORBIDDEN, "Usuário inativo");
    }
}