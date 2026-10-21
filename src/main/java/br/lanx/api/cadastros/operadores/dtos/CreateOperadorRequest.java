package br.lanx.api.cadastros.operadores.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOperadorRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String nome,
        UUID departamentoId) {
}
