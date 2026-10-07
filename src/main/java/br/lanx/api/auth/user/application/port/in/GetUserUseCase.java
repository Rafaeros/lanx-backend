package br.lanx.api.auth.user.application.port.in;

import br.lanx.api.auth.permission.application.port.in.PermissionsView;

import java.util.List;
import java.util.UUID;

public interface GetUserUseCase {
    UserView user(UUID id);

    /** {@code effective} é o resultado final: roles + grants − denies. */
    record UserView(
            UUID id,
            String username,
            boolean active,
            List<RoleSummary> roles,
            PermissionsView grants,
            PermissionsView denies,
            PermissionsView effective) {
    }

    record RoleSummary(UUID id, String name) {
    }
}
