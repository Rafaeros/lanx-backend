package br.lanx.api.auth.user.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
                @NotEmpty Set<@NotNull UUID> roleIds,
                boolean active,
                /** Nulo tira o usuário do departamento. */
                UUID departamentoId) {
}
