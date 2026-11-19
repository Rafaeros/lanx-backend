package br.lanx.api.rh.cadastros.colaboradores.dtos;

import java.util.UUID;

import br.lanx.api.rh.cadastros.colaboradores.entities.Colaborador;

public record ColaboradorResponse(
    UUID id,
    String nome,
    String departamentoNome) {
    public static ColaboradorResponse fromEntity(Colaborador c) {
        return new ColaboradorResponse(c.getId(), c.getNome(), c.getDepartamento().getNome());
    }
}
