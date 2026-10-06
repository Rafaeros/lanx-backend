package br.lanx.api.modules.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.modules.auth.domain.User;
import br.lanx.api.modules.auth.domain.UserRepository;
import br.lanx.api.modules.auth.domain.exceptions.InvalidCredentialException;

@Service
public class LoginService {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginService(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand cmd) {
        User user = users.findByUsername(cmd.username())
                .filter(u -> passwordHasher.matches(cmd.password(), u.passwordHash()))
                .orElseThrow(InvalidCredentialException::new);

        user.ensureCanLogin();

        TokenIssuer.Token token = tokenIssuer.issue(user);
        return new LoginResult(token.value(), token.expiresAt());
    }

}
