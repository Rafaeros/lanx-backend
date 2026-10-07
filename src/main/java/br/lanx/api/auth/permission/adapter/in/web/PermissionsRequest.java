package br.lanx.api.auth.permission.adapter.in.web;

import java.util.Map;
import java.util.Set;

import br.lanx.api.auth.permission.application.port.in.PermissionsInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Permissões no corpo da requisição, por nome:
 * {@code {"crud": {"compras.analise": ["READ","UPDATE"]}, "actions": {"compras.analise": ["aprovar"]}}}.
 * Os dois campos são opcionais.
 */
public record PermissionsRequest(
        Map<@NotBlank String, @NotNull Set<@NotBlank String>> crud,
        Map<@NotBlank String, @NotNull Set<@NotBlank String>> actions) {

    public PermissionsInput toInput() {
        return new PermissionsInput(crud, actions);
    }

    public static PermissionsInput toInput(PermissionsRequest request) {
        return request == null ? PermissionsInput.empty() : request.toInput();
    }
}
