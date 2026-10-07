package br.lanx.api.auth.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.user.application.port.in.VerifyCredentialsUseCase;
import br.lanx.api.auth.user.application.port.out.PasswordHasher;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.auth.user.domain.exception.InvalidCredentialException;

@Service
public class VerifyCredentialsService implements VerifyCredentialsUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public VerifyCredentialsService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional(readOnly = true)
    public VerifiedUser verify(String username, String rawPassword) {
        User user = users.findByUsername(User.normalizeUsername(username))
                .filter(u -> passwordHasher.matches(rawPassword, u.passwordHash()))
                .orElseThrow(InvalidCredentialException::new);

        // só depois da senha correta: conta inativa não vaza para quem erra a senha
        user.ensureCanLogin();

        return new VerifiedUser(user.id(), user.username());
    }
}
