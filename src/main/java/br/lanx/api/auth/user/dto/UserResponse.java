package br.lanx.api.auth.user.dto;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import br.lanx.api.auth.user.User;

/**
 * {@code permissions} é o resultado final: a união das permissões das roles.
 * {@code firstAccess}: ainda não trocou a senha provisória.
 * {@code departamento} é nulo para quem não tem departamento.
 */
public record UserResponse(UUID id, String username, boolean active, boolean firstAccess,
                DepartamentoSummary departamento, List<RoleSummary> roles, List<String> permissions) {

        public record RoleSummary(UUID id, String name) {
        }

        public record DepartamentoSummary(UUID id, String nome) {
        }

        public static UserResponse from(User user) {
                return new UserResponse(
                                user.getId(),
                                user.getUsername(),
                                user.isActive(),
                                user.isFirstAccess(),
                                user.getDepartamento() == null ? null
                                                : new DepartamentoSummary(user.getDepartamento().getId(),
                                                                user.getDepartamento().getNome()),
                                user.getRoles().stream()
                                                .sorted(Comparator.comparing(r -> r.getName()))
                                                .map(r -> new RoleSummary(r.getId(), r.getName()))
                                                .toList(),
                                user.permissionCodes().stream().sorted().toList());
        }
}
