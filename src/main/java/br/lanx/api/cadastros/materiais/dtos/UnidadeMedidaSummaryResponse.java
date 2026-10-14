package br.lanx.api.cadastros.materiais.dtos;

import java.util.UUID;

public record UnidadeMedidaSummaryResponse(
        UUID id,
        String nome,
        String sigla) {
}
