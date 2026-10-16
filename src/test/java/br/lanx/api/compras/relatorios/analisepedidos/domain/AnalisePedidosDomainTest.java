package br.lanx.api.compras.relatorios.analisepedidos.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class AnalisePedidosDomainTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 10, 1);

    @Test
    void situacaoSegueQuantidadeAtendidaESaldo() {
        assertThat(Situacao.de(null, new BigDecimal("10"))).isEqualTo(Situacao.PENDENTE);
        assertThat(Situacao.de(BigDecimal.ZERO, new BigDecimal("10"))).isEqualTo(Situacao.PENDENTE);
        assertThat(Situacao.de(new BigDecimal("4"), new BigDecimal("6"))).isEqualTo(Situacao.PARCIAL);
        assertThat(Situacao.de(new BigDecimal("10"), BigDecimal.ZERO)).isEqualTo(Situacao.ATENDIDO);
        assertThat(Situacao.de(new BigDecimal("10"), null)).isEqualTo(Situacao.ATENDIDO);
    }

    @Test
    void leadTimeSoExisteComEntrega() {
        assertThat(LeadTime.emDias(DIA_1, DIA_1.plusDays(15))).isEqualTo(15L);
        assertThat(LeadTime.emDias(DIA_1, null)).isNull();
    }

    @Test
    void resumoSomaValoresContaSituacoesEMediaLeadTimeDosEntregues() {
        var resumo = ResumoAnalisePedidos.de(List.of(
                new IndicadoresItem(new BigDecimal("100.50"), BigDecimal.ZERO, new BigDecimal("5"), DIA_1, null),
                new IndicadoresItem(new BigDecimal("200"), new BigDecimal("2"), new BigDecimal("3"), DIA_1, DIA_1.plusDays(10)),
                new IndicadoresItem(null, new BigDecimal("5"), BigDecimal.ZERO, DIA_1, DIA_1.plusDays(21))));

        assertThat(resumo.totalItens()).isEqualTo(3);
        assertThat(resumo.valorTotal()).isEqualByComparingTo("300.50");
        assertThat(resumo.pendentes()).isEqualTo(1);
        assertThat(resumo.parciais()).isEqualTo(1);
        assertThat(resumo.atendidos()).isEqualTo(1);
        assertThat(resumo.leadTimeMedioDias()).isEqualTo(16L); // (10 + 21) / 2 = 15.5 -> 16
    }

    @Test
    void resumoVazioNaoTemLeadTime() {
        var resumo = ResumoAnalisePedidos.de(List.of());
        assertThat(resumo.totalItens()).isZero();
        assertThat(resumo.leadTimeMedioDias()).isNull();
    }

    @Test
    void consultaExigePeriodoValido() {
        assertThatThrownBy(() -> new ConsultaAnalisePedidos(DIA_1.plusDays(1), DIA_1, null, null))
                .isInstanceOf(FiltroInvalidoException.class);
        assertThatThrownBy(() -> new ConsultaAnalisePedidos(null, DIA_1, null, null))
                .isInstanceOf(FiltroInvalidoException.class);
        assertThat(new ConsultaAnalisePedidos(DIA_1, DIA_1, "  pc-1 ", null).busca()).isEqualTo("pc-1");
    }
}
