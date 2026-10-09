package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import jakarta.validation.constraints.Size;

public record SearchUnidadeMedidaRequest(String filtro, int page, @Size(min = 1, max = 100) int size) {

}
