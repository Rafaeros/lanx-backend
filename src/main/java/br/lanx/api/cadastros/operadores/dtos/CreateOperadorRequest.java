package br.lanx.api.cadastros.operadores.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOperadorRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String nome,
        UUID departamentoId,
        /** Usuário do LANX (1 usuário = no máximo 1 operador); nulo deixa (ou volta) o operador sem usuário. */
        UUID userId) {
}
