package br.lanx.api.compras.relatorios.iqf.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AvaliacaoPrazoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 19);

    @ParameterizedTest(name = "previsão {0}, entrega {1} -> {2}")
    @CsvSource(nullValues = "-", value = {
            // entregue antes, no dia ou 1 dia depois da previsão: no prazo (tolerância)
            "2026-09-10, 2026-09-08, NO_PRAZO",
            "2026-09-10, 2026-09-10, NO_PRAZO",
            "2026-09-10, 2026-09-11, NO_PRAZO",
            // 2 dias depois: atrasada
            "2026-09-10, 2026-09-12, ATRASADA",
            // não entregue: atrasada só depois da tolerância
            "2026-09-10, -, ATRASADA",
            "2026-10-18, -, PENDENTE",
            "2026-10-19, -, PENDENTE",
            // previsão futura não conta ainda, mesmo já entregue adiantado
            "2026-11-05, 2026-10-02, PENDENTE",
            "2026-11-05, -, PENDENTE",
            // sem previsão não dá para avaliar
            "-, 2026-10-02, NAO_AVALIAVEL" })
    void avalia(LocalDate previsao, LocalDate entrega, AvaliacaoPrazo esperado) {
        assertThat(AvaliacaoPrazo.avaliar(previsao, entrega, HOJE)).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "tolerância {3}: previsão {0}, entrega {1} -> {2}")
    @CsvSource(nullValues = "-", value = {
            // exemplo do negócio: previsão hoje (19/10), entrega 23/10, tolerância 5 -> no prazo
            "2026-10-19, 2026-10-23, NO_PRAZO, 5",
            "2026-10-19, 2026-10-24, NO_PRAZO, 5",
            "2026-10-19, 2026-10-25, ATRASADA, 5",
            // sem tolerância, 1 dia já é atraso
            "2026-09-10, 2026-09-11, ATRASADA, 0",
            // não entregue: só atrasa depois da tolerância
            "2026-10-15, -, PENDENTE, 5",
            "2026-10-13, -, ATRASADA, 5" })
    void avaliaComToleranciaEscolhida(LocalDate previsao, LocalDate entrega, AvaliacaoPrazo esperado, int tolerancia) {
        assertThat(AvaliacaoPrazo.avaliar(previsao, entrega, HOJE, tolerancia)).isEqualTo(esperado);
    }
}
