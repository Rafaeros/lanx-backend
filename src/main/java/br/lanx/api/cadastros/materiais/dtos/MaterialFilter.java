package br.lanx.api.cadastros.materiais.dtos;

import br.lanx.api.cadastros.materiais.enums.TipoMaterial;

public record MaterialFilter(
        String codigo,
        String descricao,
        /** Ausente = todos os tipos. */
        TipoMaterial tipo) {
    public boolean hasCodigo() {
        return codigo != null && !codigo.isBlank();
    }

    public boolean hasDescricao() {
        return descricao != null && !descricao.isBlank();
    }
}