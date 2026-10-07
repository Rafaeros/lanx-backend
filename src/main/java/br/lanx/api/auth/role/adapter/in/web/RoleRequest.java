package br.lanx.api.auth.role.adapter.in.web;

import br.lanx.api.auth.permission.adapter.in.web.PermissionsRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo para criar e para editar uma role. */
public record RoleRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull @Valid PermissionsRequest permissions) {
}
