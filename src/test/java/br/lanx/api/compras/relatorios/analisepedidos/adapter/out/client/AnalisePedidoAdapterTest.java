package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;

class AnalisePedidoAdapterTest {

    private static FiltroPedidos periodo(String inicio, String fim) {
        return new FiltroPedidos(LocalDate.parse(inicio), LocalDate.parse(fim));
    }

    @Test
    void divideUmPeriodoLongoEmMesesSemSobreposicao() {
        assertThat(AnalisePedidoAdapter.dividirPorMes(periodo("2026-01-15", "2026-03-10"))).containsExactly(
                periodo("2026-01-15", "2026-01-31"),
                periodo("2026-02-01", "2026-02-28"),
                periodo("2026-03-01", "2026-03-10"));
    }

    @Test
    void periodoDentroDeUmMesFicaInteiro() {
        assertThat(AnalisePedidoAdapter.dividirPorMes(periodo("2026-09-01", "2026-09-30")))
                .containsExactly(periodo("2026-09-01", "2026-09-30"));
    }

    @Test
    void semestreViraSeisMeses() {
        assertThat(AnalisePedidoAdapter.dividirPorMes(periodo("2026-01-01", "2026-06-30"))).hasSize(6);
    }
}
