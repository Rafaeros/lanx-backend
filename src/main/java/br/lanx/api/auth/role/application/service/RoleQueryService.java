package br.lanx.api.auth.role.application.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.application.port.in.PermissionsView;
import br.lanx.api.auth.role.application.port.in.FindRolesUseCase;
import br.lanx.api.auth.role.application.port.in.GetRolesUseCase;
import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.role.domain.exception.RoleNotFoundException;

@Service
public class RoleQueryService implements GetRolesUseCase, FindRolesUseCase {

    private final RoleRepository roles;

    public RoleQueryService(RoleRepository roles) {
        this.roles = roles;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleView> roles() {
        return roles.findAll().stream()
                .sorted(Comparator.comparing(r -> r.name()))
                .map(RoleQueryService::view)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleView role(UUID id) {
        return roles.findById(id).map(RoleQueryService::view)
                .orElseThrow(() -> new RoleNotFoundException(id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByIds(Collection<UUID> ids) {
        return roles.findByIdIn(ids);
    }

    private static RoleView view(Role role) {
        return new RoleView(role.id(), role.name(), PermissionsView.from(role.permissions()));
    }
}
