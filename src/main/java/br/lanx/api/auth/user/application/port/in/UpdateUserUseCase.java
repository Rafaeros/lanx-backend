package br.lanx.api.auth.user.application.port.in;

import br.lanx.api.auth.permission.application.port.in.PermissionsInput;

import java.util.Set;
import java.util.UUID;

public interface UpdateUserUseCase {
    void update(UpdateUserCommand command);

    record UpdateUserCommand(
            UUID id,
            Set<UUID> roleIds,
            PermissionsInput grants,
            PermissionsInput denies,
            boolean active) {
    }
}
