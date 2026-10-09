package br.lanx.api.cadastros.unidade_medida.application.port.out;

import java.util.Optional;

import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;

public interface UnidadeMedidaRepository {
    UnidadeMedida save(UnidadeMedida unidade);

    boolean existsBySigla(String sigla);

    boolean existsByNome(String nome);

    Optional<UnidadeMedida> findByNome(String nome);

    Optional<UnidadeMedida> findBySigla(String sigla);
}
