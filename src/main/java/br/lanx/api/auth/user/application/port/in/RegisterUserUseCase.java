package br.lanx.api.auth.user.application.port.in;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.auth.permission.application.port.in.PermissionsInput;

import java.util.Set;
import java.util.UUID;

public interface RegisterUserUseCase {
    CreatedResult register(RegisterUserCommand command);

    /** grants somam ao que as roles dão; denies subtraem (deny vence). */
    record RegisterUserCommand(
            String username,
            String password,
            Set<UUID> roleIds,
            PermissionsInput grants,
            PermissionsInput denies) {
    }
}
