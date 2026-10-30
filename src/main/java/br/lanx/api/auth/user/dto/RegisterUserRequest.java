package br.lanx.api.auth.user.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A regra de mínimo e força da senha é do service (PasswordPolicy); aqui só o
 * teto, para não receber um corpo gigante.
 */
public record RegisterUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 150) String password,
        @NotEmpty Set<@NotNull UUID> roleIds,
        /** Opcional: sem departamento o usuário não recebe os avisos por departamento. */
        UUID departamentoId) {
}
