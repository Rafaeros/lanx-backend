package br.lanx.api.cadastros.unidademedida.application.port.in;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;

public interface FindUnidadeMedidaByIdUseCase {
    Optional<UnidadeMedida> findById(UUID id);
}
