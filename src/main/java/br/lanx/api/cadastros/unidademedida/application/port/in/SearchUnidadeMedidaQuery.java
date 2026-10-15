package br.lanx.api.cadastros.unidademedida.application.port.in;

import br.lanx.api.shared.pagination.PageQuery;

public record SearchUnidadeMedidaQuery(
                String filtro, PageQuery page) {
}
