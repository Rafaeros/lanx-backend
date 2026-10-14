package br.lanx.api.auth.user.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UsernameAlreadyExistsException extends DomainException {
    public UsernameAlreadyExistsException(String username) {
        super(ErrorType.CONFLICT, "Já existe um usuário com o username " + username);
    }
}
