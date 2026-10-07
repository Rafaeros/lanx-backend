package br.lanx.api.auth.role.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.application.port.out.PermissionCache;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.application.port.in.EnsureRoleUseCase;
import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.role.domain.Role;

@Service
public class EnsureRoleService implements EnsureRoleUseCase {

    private final RoleRepository roles;
    private final FeatureCatalog catalog;
    private final PermissionCache cache;

    public EnsureRoleService(RoleRepository roles, FeatureCatalog catalog, PermissionCache cache) {
        this.roles = roles;
        this.catalog = catalog;
        this.cache = cache;
    }

    @Override
    @Transactional
    public Role ensure(String name, PermissionSet permissions) {
        catalog.ensureKnown(permissions);
        String normalized = Role.normalizeName(name);

        Role saved = roles.findByName(normalized)
                .map(current -> roles.save(current.update(normalized, permissions)))
                .orElseGet(() -> roles.save(Role.create(normalized, permissions)));

        cache.invalidateAll();
        return saved;
    }
}
