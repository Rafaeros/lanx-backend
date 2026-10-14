package br.lanx.api.auth.user.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.application.port.in.PermissionsView;
import br.lanx.api.auth.role.application.port.in.FindRolesUseCase;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.user.application.port.in.FindUserUseCase;
import br.lanx.api.auth.user.application.port.in.GetUserUseCase;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.auth.user.domain.exception.UserNotFoundException;

@Service
public class UserQueryService implements GetUserUseCase, FindUserUseCase {

    private final UserRepository users;
    private final FindRolesUseCase roles;

    public UserQueryService(UserRepository users, FindRolesUseCase roles) {
        this.users = users;
        this.roles = roles;
    }

    @Override
    @Transactional(readOnly = true)
    public UserView user(UUID id) {
        User user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        List<Role> userRoles = roles.findByIds(user.roleIds());

        return new UserView(
                user.id(),
                user.username(),
                user.active(),
                userRoles.stream()
                        .sorted(Comparator.comparing(r -> r.name()))
                        .map(r -> new RoleSummary(r.id(), r.name()))
                        .toList(),
                PermissionsView.from(user.grants()),
                PermissionsView.from(user.denies()),
                PermissionsView.from(user.effectivePermissions(userRoles)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return users.findById(id);
    }
}
