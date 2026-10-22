package br.lanx.api.cadastros.materiais.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.materiais.enums.TipoMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** {@code tipo} nulo mantém o tipo atual. */
public record UpdateMaterialRequest(
        @NotBlank String codigo,
        @NotBlank String descricao,
        @NotNull UUID unidadeMedidaId,
        TipoMaterial tipo) {
}
