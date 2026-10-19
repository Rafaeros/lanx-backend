package br.lanx.api.auth.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Troca da senha provisória. A força da nova senha é regra do service
 * (PasswordPolicy); aqui só o teto, para não receber um corpo gigante.
 */
public record FirstAccessRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 150) String currentPassword,
        @NotBlank @Size(max = 150) String newPassword,
        @NotBlank @Size(max = 150) String confirmPassword) {
}
