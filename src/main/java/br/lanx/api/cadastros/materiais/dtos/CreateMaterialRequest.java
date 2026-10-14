package br.lanx.api.cadastros.materiais.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaterialRequest(
                @NotBlank String codigo,
                @NotBlank String descricao,
                @NotNull UUID unidadeMedidaId) {
}
