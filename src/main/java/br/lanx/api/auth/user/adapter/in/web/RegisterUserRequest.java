package br.lanx.api.auth.user.adapter.in.web;

import br.lanx.api.auth.permission.adapter.in.web.PermissionsRequest;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A regra de mínimo e força da senha é do domínio (PasswordPolicy); aqui só o
 * teto, para não receber um corpo gigante.
 */
public record RegisterUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 150) String password,
        @NotEmpty Set<@NotNull UUID> roleIds,
        @Valid PermissionsRequest grants,
        @Valid PermissionsRequest denies) {
}
