package br.lanx.api.cadastros.tiponaoconformidade.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.tiponaoconformidade.TipoNaoConformidade;

public record TipoNaoConformidadeResponse(
        UUID id,
        String nome) {
    public static TipoNaoConformidadeResponse fromEntity(TipoNaoConformidade e) {
        return new TipoNaoConformidadeResponse(e.getId(), e.getNome());
    }
}
