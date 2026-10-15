package br.lanx.api.auth.role;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.Permission;
import br.lanx.api.auth.permission.PermissionService;
import br.lanx.api.auth.role.dto.RoleRequest;
import br.lanx.api.auth.role.dto.RoleResponse;
import br.lanx.api.auth.role.exception.RoleAlreadyExistsException;
import br.lanx.api.auth.role.exception.RoleNotFoundException;
import br.lanx.api.shared.authorization.AuthorizationChanged;

@Service
public class RoleService {

    private final RoleRepository roles;
    private final PermissionService permissions;
    private final ApplicationEventPublisher events;

    RoleService(RoleRepository roles, PermissionService permissions, ApplicationEventPublisher events) {
        this.roles = roles;
        this.permissions = permissions;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roles.findAll().stream()
                .sorted(Comparator.comparing(r -> r.getName()))
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(UUID id) {
        return roles.findById(id).map(RoleResponse::from)
                .orElseThrow(() -> new RoleNotFoundException(id.toString()));
    }

    @Transactional
    public UUID create(RoleRequest request) {
        String name = Role.normalizeName(request.name());
        if (roles.existsByName(name)) {
            throw new RoleAlreadyExistsException(name);
        }
        Role role = new Role(name, request.description(), permissions.requireAll(request.permissions()));
        return roles.save(role).getId();
    }

    @Transactional
    public void update(UUID id, RoleRequest request) {
        Role role = roles.findById(id).orElseThrow(() -> new RoleNotFoundException(id.toString()));

        String name = Role.normalizeName(request.name());
        if (!name.equals(role.getName()) && roles.existsByName(name)) {
            throw new RoleAlreadyExistsException(name);
        }
        role.update(name, request.description(), permissions.requireAll(request.permissions()));

        // quem tem esta role mudou de permissão: o cache de authorities precisa cair
        events.publishEvent(AuthorizationChanged.everyone());
    }

    /**
     * Para o módulo user: todas as roles pedidas existem, ou falha dizendo quais
     * faltam.
     */
    @Transactional(readOnly = true)
    public Set<Role> requireAll(Collection<UUID> ids) {
        Set<UUID> requested = Set.copyOf(ids);
        List<Role> found = roles.findByIdIn(requested);

        if (found.size() != requested.size()) {
            Set<UUID> missing = new HashSet<>(requested);
            found.forEach(r -> missing.remove(r.getId()));
            throw new RoleNotFoundException(
                    missing.stream().map(u -> u.toString()).collect(Collectors.joining(", ")));
        }
        return Set.copyOf(found);
    }

    /**
     * A ADMIN existe sempre e tem TODAS as permissões do catálogo: permissão nova
     * declarada no código entra nela sozinha na próxima subida.
     */
    @Transactional
    public Role ensureAdmin(Set<Permission> everything) {
        Role admin = roles.findByName(Role.ADMIN)
                .orElseGet(() -> new Role(Role.ADMIN, "Acesso total ao sistema", everything));
        admin.grantOnly(everything);

        Role saved = roles.save(admin);
        events.publishEvent(AuthorizationChanged.everyone());
        return saved;
    }
}
