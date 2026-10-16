package br.lanx.api.auth.user;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.role.Role;
import br.lanx.api.auth.role.RoleService;
import br.lanx.api.auth.user.dto.RegisterUserRequest;
import br.lanx.api.auth.user.dto.UpdateUserRequest;
import br.lanx.api.auth.user.dto.UserResponse;
import br.lanx.api.auth.user.dto.VerifiedUser;
import br.lanx.api.auth.user.exception.InvalidCredentialException;
import br.lanx.api.auth.user.exception.UserNotFoundException;
import br.lanx.api.auth.user.exception.UsernameAlreadyExistsException;
import br.lanx.api.shared.authorization.AuthorizationChanged;

@Service
public class UserService {

    private final UserRepository users;
    private final RoleService roles;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    UserService(UserRepository users, RoleService roles, PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAllByOrderByUsernameAsc().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return users.findById(id).map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    @Transactional
    public UUID register(RegisterUserRequest request) {
        String username = User.normalizeUsername(request.username());
        if (users.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        // a senha em texto puro só é validada aqui: o User guarda apenas o hash
        PasswordPolicy.validate(request.password());

        User user = new User(username, passwordEncoder.encode(request.password()),
                roles.requireAll(request.roleIds()));
        return users.save(user).getId();
    }

    @Transactional
    public void update(UUID id, UpdateUserRequest request) {
        User user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        user.update(roles.requireAll(request.roleIds()), request.active());

        // vale na hora: o próximo request do usuário já recalcula as authorities
        events.publishEvent(AuthorizationChanged.ofUser(id));
    }

    /**
     * Confirma quem é. Usuário inexistente e senha errada dão a mesma resposta,
     * para não revelar quais contas existem.
     */
    @Transactional(readOnly = true)
    public VerifiedUser verifyCredentials(String username, String rawPassword) {
        User user = users.findByUsername(User.normalizeUsername(username))
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(InvalidCredentialException::new);

        // só depois da senha correta: conta inativa não vaza para quem erra a senha
        user.ensureCanLogin();

        return new VerifiedUser(user.getId(), user.getUsername());
    }

    /**
     * Cria o administrador se ainda não existir. Se já existe, não mexe (nem na
     * senha).
     */
    @Transactional
    public void ensureAdmin(String username, String rawPassword, Role adminRole) {
        String normalized = User.normalizeUsername(username);
        if (!users.existsByUsername(normalized)) {
            PasswordPolicy.validate(rawPassword);
            users.save(new User(normalized, passwordEncoder.encode(rawPassword), Set.of(adminRole)));
        }
    }
}
