package br.lanx.api.auth.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.application.port.in.PermissionsInput;
import br.lanx.api.auth.user.application.port.in.UpdateUserUseCase;
import br.lanx.api.auth.permission.application.port.out.PermissionCache;
import br.lanx.api.auth.role.application.port.in.FindRolesUseCase;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.auth.user.domain.exception.UserNotFoundException;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository users;
    private final FindRolesUseCase roles;
    private final FeatureCatalog catalog;
    private final PermissionCache cache;

    public UpdateUserService(UserRepository users, FindRolesUseCase roles, FeatureCatalog catalog,
            PermissionCache cache) {
        this.users = users;
        this.roles = roles;
        this.catalog = catalog;
        this.cache = cache;
    }

    @Override
    @Transactional
    public void update(UpdateUserCommand cmd) {
        User current = users.findById(cmd.id()).orElseThrow(() -> new UserNotFoundException(cmd.id().toString()));

        User updated = current.update(
                UserRoles.requireExisting(roles, cmd.roleIds()),
                known(cmd.grants()),
                known(cmd.denies()),
                cmd.active());
        users.save(updated);

        // vale na hora: o próximo request do usuário já recalcula as permissões
        cache.invalidate(updated.id());
    }

    private PermissionSet known(PermissionsInput input) {
        PermissionSet set = (input == null ? PermissionsInput.empty() : input).toDomain();
        catalog.ensureKnown(set);
        return set;
    }
}
