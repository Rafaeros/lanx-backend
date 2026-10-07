package br.lanx.api.auth.authentication.application.service;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authentication.application.port.in.LoginCommand;
import br.lanx.api.auth.authentication.application.port.in.LoginResult;
import br.lanx.api.auth.authentication.application.port.in.LoginUseCase;
import br.lanx.api.auth.authentication.application.port.out.TokenIssuer;
import br.lanx.api.auth.user.application.port.in.VerifyCredentialsUseCase;
import br.lanx.api.auth.user.application.port.in.VerifyCredentialsUseCase.VerifiedUser;

/**
 * Autenticar = confirmar quem é (módulo user) + emitir o token (este módulo).
 * Não conhece hash nem repositório de usuário.
 */
@Service
public class LoginService implements LoginUseCase {

    private final VerifyCredentialsUseCase credentials;
    private final TokenIssuer tokenIssuer;

    public LoginService(VerifyCredentialsUseCase credentials, TokenIssuer tokenIssuer) {
        this.credentials = credentials;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public LoginResult login(LoginCommand cmd) {
        VerifiedUser user = credentials.verify(cmd.username(), cmd.password());

        TokenIssuer.Token token = tokenIssuer.issue(user.id(), user.username());
        return new LoginResult(token.value(), token.expiresAt());
    }
}
