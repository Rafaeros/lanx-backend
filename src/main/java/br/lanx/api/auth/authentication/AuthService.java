package br.lanx.api.auth.authentication;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authentication.dto.LoginRequest;
import br.lanx.api.auth.authentication.dto.LoginResponse;
import br.lanx.api.auth.user.UserService;
import br.lanx.api.auth.user.dto.VerifiedUser;

/**
 * Autenticar = confirmar quem é (UserService) + emitir o token (TokenService).
 */
@Service
public class AuthService {

    private final UserService users;
    private final TokenService tokens;

    AuthService(UserService users, TokenService tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public LoginResponse login(LoginRequest request) {
        VerifiedUser user = users.verifyCredentials(request.username(), request.password());
        TokenService.Token token = tokens.issue(user.id(), user.username());
        return LoginResponse.bearer(token.value(), token.expiresAt());
    }
}
