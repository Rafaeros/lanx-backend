package br.lanx.api.cadastros.unidademedida.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidademedida.application.port.in.SearchUnidadeMedidaQuery;
import br.lanx.api.cadastros.unidademedida.application.port.in.SearchUnidadesMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class SearchUnidadeMedidaService implements SearchUnidadesMedidaUseCase {

    private final UnidadeMedidaRepository unidades;

    SearchUnidadeMedidaService(UnidadeMedidaRepository unidades) {
        this.unidades = unidades;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UnidadeMedida> search(SearchUnidadeMedidaQuery query) {
        return unidades.search(query.filtro(), query.page());
    }

}
