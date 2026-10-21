package br.lanx.api.cadastros.departamentos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDepartamentoRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String nome) {
}
