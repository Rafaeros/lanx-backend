package br.lanx.api.cadastros.materiais.dtos;

import java.util.UUID;

public record CreateMaterialRequest(
        String codigo,
        String descricao,
        UUID unidadeMedidaId) {
}
