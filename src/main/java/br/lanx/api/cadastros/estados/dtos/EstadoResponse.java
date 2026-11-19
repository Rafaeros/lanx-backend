package br.lanx.api.cadastros.estados.dtos;

import br.lanx.api.cadastros.estados.Estado;

public record EstadoResponse(
        Integer codigoUf,
        String uf,
        String nome) {
    public static EstadoResponse fromEntity(Estado e) {
        return new EstadoResponse(e.getCodigoUf(), e.getUf(), e.getNome());
    }
}
