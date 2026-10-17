package br.lanx.api.cadastros.pais.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.pais.Pais;

public record PaisResponse(
        UUID id,
        String nome,
        String codigoIso2,
        String codigoIso3) {

    public static PaisResponse fromEntity(Pais pais) {
        return new PaisResponse(pais.getId(), pais.getNome(), pais.getCodigoIso2(), pais.getCodigoIso3());
    }
}
