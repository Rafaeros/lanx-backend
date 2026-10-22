package br.lanx.api.cadastros.materiais.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.materiais.enums.TipoMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** {@code tipo} nulo = MATERIA_PRIMA (o padrão da importação de compras). */
public record CreateMaterialRequest(
                @NotBlank String codigo,
                @NotBlank String descricao,
                @NotNull UUID unidadeMedidaId,
                TipoMaterial tipo) {
}
