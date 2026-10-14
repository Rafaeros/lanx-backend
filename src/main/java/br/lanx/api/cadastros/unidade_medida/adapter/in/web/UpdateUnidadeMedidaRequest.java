package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import java.util.UUID;

public record UpdateUnidadeMedidaRequest(UUID id, String nome, String sigla) {
}
