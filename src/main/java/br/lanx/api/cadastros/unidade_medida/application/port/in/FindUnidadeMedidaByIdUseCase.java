package br.lanx.api.cadastros.unidade_medida.application.port.in;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;

public interface FindUnidadeMedidaByIdUseCase {
    Optional<UnidadeMedida> findById(UUID id);
}
