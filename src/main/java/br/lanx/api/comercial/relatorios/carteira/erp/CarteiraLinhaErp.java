package br.lanx.api.comercial.relatorios.carteira.erp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Linha de GET /relatorios/carteira do cg-scraper (snake_case; decimais vêm
 * como texto, ex.: "1.0000"). {@code ordem} é o código da OP.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarteiraLinhaErp(
                Integer negociacao,
                String pedidoCliente,
                String ordem,
                String tipoServico,
                String cliente,
                String codigo,
                String descricao,
                LocalDate previsao,
                BigDecimal pendente,
                BigDecimal valorTotal,
                String etapaAtual,
                List<MaterialPendenteErp> materiaisPendentes) {

        public CarteiraLinhaErp {
                if (etapaAtual == null || etapaAtual.isBlank()) {
                        etapaAtual = "A FATURAR";
                }
        }

        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record MaterialPendenteErp(
                        String codigo,
                        String descricao,
                        BigDecimal quantidadePendente,
                        String unidadeMedida,
                        LocalDate previsao) {
        }
}
