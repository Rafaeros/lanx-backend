package br.lanx.api.auth.role.application.port.in;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.auth.permission.application.port.in.PermissionsInput;

public interface RegisterRoleUseCase {
    CreatedResult register(RegisterRoleCommand command);

    record RegisterRoleCommand(String name, PermissionsInput permissions) {
    }
}
