package br.lanx.api.auth.role.application.port.in;

import br.lanx.api.auth.permission.application.port.in.PermissionsInput;

import java.util.UUID;

public interface UpdateRoleUseCase {
    void update(UpdateRoleCommand command);

    record UpdateRoleCommand(UUID id, String name, PermissionsInput permissions) {
    }
}
