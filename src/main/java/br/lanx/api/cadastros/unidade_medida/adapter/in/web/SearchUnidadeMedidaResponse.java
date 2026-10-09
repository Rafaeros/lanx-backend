package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;

public record SearchUnidadeMedidaResponse(
                UUID id, String nome, String sigla, BigDecimal fatorConversao) {
        static SearchUnidadeMedidaResponse from(UnidadeMedida dominio) {
                return new SearchUnidadeMedidaResponse(dominio.id(), dominio.nome(), dominio.sigla(),
                                dominio.fatorConversao());
        }
}