package br.lanx.api.cadastros.unidademedida.adapter.in.web;

import java.util.UUID;

public record UpdateUnidadeMedidaRequest(UUID id, String nome, String sigla) {
}
