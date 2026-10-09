package br.lanx.api.cadastros.unidade_medida.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

public interface UnidadeMedidaRepository {
    UnidadeMedida save(UnidadeMedida unidade);

    PageResult<UnidadeMedida> search(String filtro, PageQuery query);

    boolean existsById(UUID id);

    boolean existsBySigla(String sigla);

    boolean existsByNome(String nome);

    Optional<UnidadeMedida> findByNome(String nome);

    Optional<UnidadeMedida> findBySigla(String sigla);

    Optional<UnidadeMedida> findById(UUID id);

    void delete(UUID id);
}
