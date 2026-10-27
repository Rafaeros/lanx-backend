package br.lanx.api.qualidade.relatorios.iqf.adapter.in.web;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import br.lanx.api.qualidade.relatorios.iqf.domain.AvaliacaoPrazo;

/**
 * Query params de GET /qualidade/relatorios/iqf (e /exportar). O período é o da
 * previsão de entrega do PC; sem datas, vale o mês corrente. {@code rateios}
 * são ids de cadastros.rateios e se repetem na URL (rateios=id1&rateios=id2);
 * ausente = todos. {@code toleranciaDias}
 * (0 a 30, padrão 1): dias de atraso após a previsão que ainda contam no prazo.
 */
public record ConsultarIqfRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        List<UUID> rateios,
        Integer toleranciaDias) {

    LocalDate inicio() {
        return dataInicio != null ? dataInicio : YearMonth.now().atDay(1);
    }

    LocalDate fim() {
        return dataFim != null ? dataFim : YearMonth.now().atEndOfMonth();
    }

    int tolerancia() {
        return toleranciaDias != null ? toleranciaDias : AvaliacaoPrazo.TOLERANCIA_PADRAO_DIAS;
    }

    Set<UUID> rateiosSelecionados() {
        return rateios == null ? Set.of() : Set.copyOf(rateios);
    }
}
