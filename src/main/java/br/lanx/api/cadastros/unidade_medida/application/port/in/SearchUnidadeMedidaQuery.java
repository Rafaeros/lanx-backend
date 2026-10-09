package br.lanx.api.cadastros.unidade_medida.application.port.in;

import br.lanx.api.shared.pagination.PageQuery;

public record SearchUnidadeMedidaQuery(
                String filtro, PageQuery page) {
}
