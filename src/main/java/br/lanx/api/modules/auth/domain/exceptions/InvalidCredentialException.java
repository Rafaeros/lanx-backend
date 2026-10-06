package br.lanx.api.modules.auth.domain.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class InvalidCredentialException extends DomainException {
    public InvalidCredentialException() {
        super(ErrorType.UNAUTHENTICATED, "Usuário ou senha inválidos");
    }
}