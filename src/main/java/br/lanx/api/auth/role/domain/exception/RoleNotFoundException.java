package br.lanx.api.auth.role.domain.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class RoleNotFoundException extends DomainException {
    public RoleNotFoundException(String reference) {
        super(ErrorType.NOT_FOUND, "Role não encontrada: " + reference);
    }
}
