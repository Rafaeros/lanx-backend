package br.lanx.api.comercial.relatorios.carteira.dtos;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Query params de GET /comercial/relatorios/carteira (e /exportar). Sem datas,
 * vale o mês corrente. {@code atualizar=true} ignora o cache e consulta o ERP de novo.
 */
public record CarteiraRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        Boolean atualizar) {

    public LocalDate inicio() {
        return dataInicio != null ? dataInicio : YearMonth.now().atDay(1);
    }

    public LocalDate fim() {
        return dataFim != null ? dataFim : YearMonth.now().atEndOfMonth();
    }

    public boolean forcarAtualizacao() {
        return Boolean.TRUE.equals(atualizar);
    }
}
