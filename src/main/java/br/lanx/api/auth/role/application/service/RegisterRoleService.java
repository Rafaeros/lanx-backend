package br.lanx.api.auth.role.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.auth.role.application.port.in.RegisterRoleUseCase;
import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.role.domain.exception.RoleAlreadyExistsException;

@Service
public class RegisterRoleService implements RegisterRoleUseCase {

    private final RoleRepository roles;
    private final FeatureCatalog catalog;

    public RegisterRoleService(RoleRepository roles, FeatureCatalog catalog) {
        this.roles = roles;
        this.catalog = catalog;
    }

    @Override
    @Transactional
    public CreatedResult register(RegisterRoleCommand cmd) {
        String name = Role.normalizeName(cmd.name());
        if (roles.existsByName(name)) {
            throw new RoleAlreadyExistsException(name);
        }

        PermissionSet permissions = cmd.permissions().toDomain();
        catalog.ensureKnown(permissions);

        Role saved = roles.save(Role.create(name, permissions));
        return new CreatedResult(saved.id());
    }
}
