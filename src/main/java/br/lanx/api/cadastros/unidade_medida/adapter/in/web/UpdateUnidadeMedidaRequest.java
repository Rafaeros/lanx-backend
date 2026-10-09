package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateUnidadeMedidaRequest(UUID id, String nome, String sigla, BigDecimal fatorConversao) {
}
