package br.lanx.api.auth.authorization.domain.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class PermissionDeniedException extends DomainException {
    public PermissionDeniedException() {
        super(ErrorType.FORBIDDEN, "Sem permissão para este recurso");
    }
}
