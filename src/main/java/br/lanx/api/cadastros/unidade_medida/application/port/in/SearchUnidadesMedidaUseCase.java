package br.lanx.api.cadastros.unidade_medida.application.port.in;

import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;
import br.lanx.api.shared.pagination.PageResult;

public interface SearchUnidadesMedidaUseCase {
    PageResult<UnidadeMedida> search(SearchUnidadeMedidaQuery query);

}
