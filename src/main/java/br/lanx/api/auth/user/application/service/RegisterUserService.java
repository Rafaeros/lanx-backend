package br.lanx.api.auth.user.application.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.auth.permission.application.port.in.PermissionsInput;
import br.lanx.api.auth.user.application.port.in.RegisterUserUseCase;
import br.lanx.api.auth.user.application.port.out.PasswordHasher;
import br.lanx.api.auth.role.application.port.in.FindRolesUseCase;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.user.domain.PasswordPolicy;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.auth.user.domain.exception.UsernameAlreadyExistsException;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository users;
    private final FindRolesUseCase roles;
    private final PasswordHasher passwordHasher;
    private final FeatureCatalog catalog;

    public RegisterUserService(UserRepository users, FindRolesUseCase roles, PasswordHasher passwordHasher,
            FeatureCatalog catalog) {
        this.users = users;
        this.roles = roles;
        this.passwordHasher = passwordHasher;
        this.catalog = catalog;
    }

    @Override
    @Transactional
    public CreatedResult register(RegisterUserCommand cmd) {
        String username = User.normalizeUsername(cmd.username());
        if (users.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        // a senha em texto puro só é validada aqui: o User guarda apenas o hash
        PasswordPolicy.validate(cmd.password());

        Set<UUID> roleIds = UserRoles.requireExisting(roles, cmd.roleIds());
        PermissionSet grants = known(cmd.grants());
        PermissionSet denies = known(cmd.denies());

        User user = User.create(username, passwordHasher.hash(cmd.password()), roleIds, grants, denies);
        return new CreatedResult(users.save(user).id());
    }

    private PermissionSet known(PermissionsInput input) {
        PermissionSet set = (input == null ? PermissionsInput.empty() : input).toDomain();
        catalog.ensureKnown(set);
        return set;
    }
}
