package br.lanx.api.auth.user.dto;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import br.lanx.api.auth.user.User;

/**
 * {@code permissions} é o resultado final: a união das permissões das roles.
 * {@code firstAccess}: ainda não trocou a senha provisória.
 */
public record UserResponse(UUID id, String username, boolean active, boolean firstAccess,
                List<RoleSummary> roles, List<String> permissions) {

        public record RoleSummary(UUID id, String name) {
        }

        public static UserResponse from(User user) {
                return new UserResponse(
                                user.getId(),
                                user.getUsername(),
                                user.isActive(),
                                user.isFirstAccess(),
                                user.getRoles().stream()
                                                .sorted(Comparator.comparing(r -> r.getName()))
                                                .map(r -> new RoleSummary(r.getId(), r.getName()))
                                                .toList(),
                                user.permissionCodes().stream().sorted().toList());
        }
}
