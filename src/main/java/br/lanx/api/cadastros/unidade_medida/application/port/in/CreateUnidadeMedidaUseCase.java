package br.lanx.api.cadastros.unidade_medida.application.port.in;

import java.math.BigDecimal;

import br.lanx.api.shared.application.CreatedResult;

public interface CreateUnidadeMedidaUseCase {
    CreatedResult create(CreateUnidadeMedidaCommand command);

    record CreateUnidadeMedidaCommand(
            String nome,
            String sigla,
            BigDecimal fatorConversao) {
    }

}
