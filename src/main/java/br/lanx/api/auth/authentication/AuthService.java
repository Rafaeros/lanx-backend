package br.lanx.api.auth.authentication;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authentication.dto.FirstAccessRequest;
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
        if (user.firstAccess()) {
            // senha provisória não ganha token: só serve para trocar a senha
            return LoginResponse.firstAccessRequired();
        }
        return issue(user);
    }

    /** Define a senha do primeiro acesso e já entra no sistema. */
    public LoginResponse completeFirstAccess(FirstAccessRequest request) {
        VerifiedUser user = users.completeFirstAccess(request.username(), request.currentPassword(),
                request.newPassword(), request.confirmPassword());
        return issue(user);
    }

    private LoginResponse issue(VerifiedUser user) {
        TokenService.Token token = tokens.issue(user.id(), user.username());
        return LoginResponse.bearer(token.value(), token.expiresAt());
    }
}
