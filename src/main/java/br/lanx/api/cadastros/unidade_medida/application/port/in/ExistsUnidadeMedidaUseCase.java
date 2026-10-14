package br.lanx.api.cadastros.unidade_medida.application.port.in;

import java.util.UUID;

public interface ExistsUnidadeMedidaUseCase {
    boolean existsById(UUID id);
}