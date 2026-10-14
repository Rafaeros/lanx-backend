package br.lanx.api.auth.permission.exception;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class UnknownPermissionException extends DomainException {
    public UnknownPermissionException(String codes) {
        super(ErrorType.VALIDATION, "Permissões desconhecidas: " + codes);
    }
}
