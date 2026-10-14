package br.lanx.api.cadastros.unidade_medida.application.port.in;

import java.util.UUID;

public interface UpdateUnidadeMedidaUseCase {
    void update(UpdateUnidadeMedidaCommand command);

    record UpdateUnidadeMedidaCommand(UUID id, String novoNome, String novaSigla) {
    }
}
