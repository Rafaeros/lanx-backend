package br.lanx.api.cadastros.tiponaoconformidade.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTipoNaoConformidadeRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 200) String nome) {
}
