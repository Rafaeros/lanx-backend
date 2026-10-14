package br.lanx.api.auth.role.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class RoleAlreadyExistsException extends DomainException {
    public RoleAlreadyExistsException(String name) {
        super(ErrorType.CONFLICT, "Já existe uma role com o nome " + name);
    }
}
