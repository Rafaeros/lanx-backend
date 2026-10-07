package br.lanx.api.auth.role.application.port.in;

import br.lanx.api.auth.permission.application.port.in.PermissionsView;

import java.util.List;
import java.util.UUID;

public interface GetRolesUseCase {
    List<RoleView> roles();

    RoleView role(UUID id);

    record RoleView(UUID id, String name, PermissionsView permissions) {
    }
}
