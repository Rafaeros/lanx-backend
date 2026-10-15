package br.lanx.api.cadastros.unidademedida.adapter.in.web;

import java.util.UUID;

import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;

public record SearchUnidadeMedidaResponse(
                UUID id, String nome, String sigla) {
        static SearchUnidadeMedidaResponse from(UnidadeMedida dominio) {
                return new SearchUnidadeMedidaResponse(dominio.id(), dominio.nome(), dominio.sigla());
        }
}