package br.lanx.api.auth.user.application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.application.port.in.EnsureRoleUseCase;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.user.application.port.in.BootstrapAdminUseCase;
import br.lanx.api.auth.user.application.port.out.PasswordHasher;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.user.domain.PasswordPolicy;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.shared.authorization.FeatureKey;

@Service
public class AdminBootstrapService implements BootstrapAdminUseCase {

    static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository users;
    private final EnsureRoleUseCase roles;
    private final PasswordHasher passwordHasher;
    private final FeatureCatalog catalog;

    public AdminBootstrapService(UserRepository users, EnsureRoleUseCase roles, PasswordHasher passwordHasher,
            FeatureCatalog catalog) {
        this.users = users;
        this.roles = roles;
        this.passwordHasher = passwordHasher;
        this.catalog = catalog;
    }

    /**
     * A role ADMIN é re-sincronizada a cada subida: funcionalidade nova declarada
     * no código entra sozinha nela. O usuário só é criado se ainda não existir.
     */
    @Override
    @Transactional
    public void ensureAdmin(String username, String rawPassword) {
        Role admin = roles.ensure(ADMIN_ROLE, everything());

        String normalized = User.normalizeUsername(username);
        if (!users.existsByUsername(normalized)) {
            PasswordPolicy.validate(rawPassword);
            users.save(User.create(normalized, passwordHasher.hash(rawPassword), Set.of(admin.id())));
        }
    }

    private PermissionSet everything() {
        Map<FeatureKey, Integer> masks = new HashMap<>();
        Map<FeatureKey, Set<String>> actions = new HashMap<>();
        catalog.all().forEach(def -> {
            masks.put(def.key(), Action.fullMask());
            actions.put(def.key(), def.extraActions());
        });
        return new PermissionSet(masks, actions);
    }
}
