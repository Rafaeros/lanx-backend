package br.lanx.api.comercial.relatorios.carteira.erp;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class CarteiraErpRespostaTest {

    /** Trecho real do cg-scraper: snake_case, decimais como texto e previsão nula no material. */
    private static final String JSON = """
            {"success": true, "data": [{
              "negociacao": 5343, "pedido_cliente": "542", "ordem": "OP-0008365", "tipo_servico": "Venda",
              "cliente": "MANP TECNOLOGIA LTDA - AGRONYX", "codigo": "AGX014 001 000",
              "descricao": "CHICOTE MONITORAMENTO PARA 7 LINHAS(M1347)", "previsao": "2026-11-27",
              "pendente": "3.0000", "valor_total": "1685.25", "etapa_atual": "CORTE",
              "materiais_pendentes": [{"codigo": "BACKCPC37MC01", "descricao": "PRESILHA",
                "quantidade_pendente": "1", "unidade_medida": "un", "previsao": null}],
              "campo_novo": "ignorado"
            }]}
            """;

    @Test
    void leALinhaDoScraper() {
        var resposta = JsonMapper.builder().build().readValue(JSON, CarteiraErpResposta.class);

        var linha = resposta.data().getFirst();
        assertThat(linha.negociacao()).isEqualTo(5343);
        assertThat(linha.pedidoCliente()).isEqualTo("542");
        assertThat(linha.ordem()).isEqualTo("OP-0008365");
        assertThat(linha.previsao()).isEqualTo(LocalDate.of(2026, 11, 27));
        assertThat(linha.pendente()).isEqualByComparingTo("3");
        assertThat(linha.valorTotal()).isEqualTo(new BigDecimal("1685.25"));
        assertThat(linha.etapaAtual()).isEqualTo("CORTE");
        var material = linha.materiaisPendentes().getFirst();
        assertThat(material.quantidadePendente()).isEqualByComparingTo("1");
        assertThat(material.unidadeMedida()).isEqualTo("un");
        assertThat(material.previsao()).isNull();
    }
}
