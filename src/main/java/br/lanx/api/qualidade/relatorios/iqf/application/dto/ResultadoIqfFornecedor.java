
package br.lanx.api.qualidade.relatorios.iqf.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ResultadoIqfFornecedor(
        UUID fornecedorId,
        String fornecedor,
        long entregasAvaliadas,
        long entregasNoPrazo,
        long entregasAtrasadas,
        BigDecimal percentualNoPrazo
) {
}
