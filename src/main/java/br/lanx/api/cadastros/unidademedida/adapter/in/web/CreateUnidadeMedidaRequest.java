package br.lanx.api.cadastros.unidademedida.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record CreateUnidadeMedidaRequest(
        @NotBlank String nome,
        @NotBlank String sigla) {
}
