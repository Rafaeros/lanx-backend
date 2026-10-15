package br.lanx.api.cadastros.unidademedida.application.port.in;

import java.util.UUID;

public interface ExistsUnidadeMedidaUseCase {
    boolean existsById(UUID id);
}