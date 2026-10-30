package br.lanx.api.auth.user;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
import br.lanx.api.auth.user.exception.FirstAccessAlreadyCompletedException;
import br.lanx.api.auth.user.exception.InvalidCredentialException;
import br.lanx.api.auth.user.exception.InvalidUserException;
import br.lanx.api.auth.user.exception.UserNotFoundException;
import br.lanx.api.auth.user.exception.UsernameAlreadyExistsException;
import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.departamentos.DepartamentoService;
import br.lanx.api.shared.authorization.AuthorizationChanged;

@Service
public class UserService {

    private final UserRepository users;
    private final RoleService roles;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final DepartamentoService departamentos;

    UserService(UserRepository users, RoleService roles, PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events, DepartamentoService departamentos) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
        this.departamentos = departamentos;
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
        // a senha do cadastro é provisória: o usuário define a dele no primeiro login
        user.requireFirstAccess();
        user.assignDepartamento(departamentoOuNulo(request.departamentoId()));
        return users.save(user).getId();
    }

    @Transactional(readOnly = true)
    public List<UUID> findActiveIdsByDepartamentos(Collection<String> nomes) {
        return users.findActiveIdsByDepartamentoNomes(nomes.stream().map(n -> n.toLowerCase(Locale.ROOT)).toList());
    }

    @Transactional
    public void update(UUID id, UpdateUserRequest request) {
        User user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        user.update(roles.requireAll(request.roleIds()), request.active());
        user.assignDepartamento(departamentoOuNulo(request.departamentoId()));

        // vale na hora: o próximo request do usuário já recalcula as authorities
        events.publishEvent(AuthorizationChanged.ofUser(id));
    }

    private Departamento departamentoOuNulo(UUID departamentoId) {
        return departamentoId == null ? null : departamentos.findByIdOrThrow(departamentoId);
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

        return new VerifiedUser(user.getId(), user.getUsername(), user.isFirstAccess());
    }

    /**
     * Primeiro acesso: confirma a senha provisória (mesmo 401 do login) e grava
     * a senha forte escolhida pelo usuário.
     */
    @Transactional
    public VerifiedUser completeFirstAccess(String username, String currentPassword, String newPassword,
            String confirmation) {
        User user = users.findByUsername(User.normalizeUsername(username))
                .filter(u -> passwordEncoder.matches(currentPassword, u.getPasswordHash()))
                .orElseThrow(InvalidCredentialException::new);
        user.ensureCanLogin();
        if (!user.isFirstAccess()) {
            throw new FirstAccessAlreadyCompletedException();
        }
        if (!newPassword.equals(confirmation)) {
            throw new InvalidUserException("A confirmação não confere com a nova senha");
        }
        if (newPassword.equals(currentPassword)) {
            throw new InvalidUserException("A nova senha precisa ser diferente da senha provisória");
        }
        PasswordPolicy.validateStrong(newPassword, user.getUsername());

        user.completeFirstAccess(passwordEncoder.encode(newPassword));
        return new VerifiedUser(user.getId(), user.getUsername(), false);
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
