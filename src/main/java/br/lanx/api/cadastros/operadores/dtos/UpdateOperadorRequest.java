package br.lanx.api.cadastros.operadores.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Substitui os dados do operador: departamentoId nulo remove o departamento. */
public record UpdateOperadorRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String nome,
        UUID departamentoId) {
}
