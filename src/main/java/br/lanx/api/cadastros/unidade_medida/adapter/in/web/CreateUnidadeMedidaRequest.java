package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUnidadeMedidaRequest(
                @NotBlank String nome,
                @NotBlank String sigla,
                @NotNull BigDecimal fatorConversao) {
}
