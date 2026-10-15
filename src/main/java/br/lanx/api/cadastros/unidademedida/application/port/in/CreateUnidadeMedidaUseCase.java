package br.lanx.api.cadastros.unidademedida.application.port.in;

import br.lanx.api.shared.application.CreatedResult;

public interface CreateUnidadeMedidaUseCase {
    CreatedResult create(CreateUnidadeMedidaCommand command);

    record CreateUnidadeMedidaCommand(
            String nome,
            String sigla) {
    }

}
