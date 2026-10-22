package br.lanx.api.cadastros.materiais.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.materiais.enums.TipoMaterial;

public record MaterialResponse(
        UUID id,
        String codigo,
        String descricao,
        UnidadeMedidaSummaryResponse unidadeMedida,
        TipoMaterial tipo) {
    public static MaterialResponse fromEntity(Material material) {
        var unidade = material.getUnidadeMedida();
        var unidadeDto = new UnidadeMedidaSummaryResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getSigla());
        return new MaterialResponse(material.getId(), material.getCodigo(), material.getDescricao(), unidadeDto,
                material.getTipoMaterial());
    }
}
