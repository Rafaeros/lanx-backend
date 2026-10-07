package br.lanx.api.auth.user.adapter.in.web;

import br.lanx.api.auth.permission.adapter.in.web.PermissionsRequest;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotEmpty Set<@NotNull UUID> roleIds,
        @Valid PermissionsRequest grants,
        @Valid PermissionsRequest denies,
        boolean active) {
}
