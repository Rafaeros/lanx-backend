package br.lanx.api.auth.role.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Corpo para criar e editar uma role. {@code permissions} são códigos do
 * catálogo ("compras.analise:read"), os mesmos devolvidos por GET
 * /auth/features.
 */
public record RoleRequest(
                @NotBlank @Size(max = 50) String name,
                @Size(max = 255) String description,
                @NotEmpty Set<@NotBlank String> permissions) {
}
