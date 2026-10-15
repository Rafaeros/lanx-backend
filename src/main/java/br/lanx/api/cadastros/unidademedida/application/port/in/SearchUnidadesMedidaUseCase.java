package br.lanx.api.cadastros.unidademedida.application.port.in;

import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;
import br.lanx.api.shared.pagination.PageResult;

public interface SearchUnidadesMedidaUseCase {
    PageResult<UnidadeMedida> search(SearchUnidadeMedidaQuery query);

}
