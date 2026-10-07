package br.lanx.api.auth.role.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.role.application.port.in.UpdateRoleUseCase;
import br.lanx.api.auth.permission.application.port.out.PermissionCache;
import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.role.domain.exception.RoleAlreadyExistsException;
import br.lanx.api.auth.role.domain.exception.RoleNotFoundException;

@Service
public class UpdateRoleService implements UpdateRoleUseCase {

    private final RoleRepository roles;
    private final FeatureCatalog catalog;
    private final PermissionCache cache;

    public UpdateRoleService(RoleRepository roles, FeatureCatalog catalog, PermissionCache cache) {
        this.roles = roles;
        this.catalog = catalog;
        this.cache = cache;
    }

    @Override
    @Transactional
    public void update(UpdateRoleCommand cmd) {
        Role current = roles.findById(cmd.id()).orElseThrow(() -> new RoleNotFoundException(cmd.id().toString()));

        String name = Role.normalizeName(cmd.name());
        boolean renamed = !name.equals(current.name());
        if (renamed && roles.existsByName(name)) {
            throw new RoleAlreadyExistsException(name);
        }

        PermissionSet permissions = cmd.permissions().toDomain();
        catalog.ensureKnown(permissions);

        roles.save(current.update(name, permissions));

        // qualquer usuário com esta role mudou de permissão: descarta o cache todo
        cache.invalidateAll();
    }
}
