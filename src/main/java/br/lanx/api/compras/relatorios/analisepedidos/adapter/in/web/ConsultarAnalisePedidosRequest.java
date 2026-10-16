package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;

import org.springframework.format.annotation.DateTimeFormat;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroInvalidoException;
import br.lanx.api.compras.relatorios.analisepedidos.domain.Situacao;

/**
 * Query params de GET /compras/relatorios/analise-pedidos (e /resumo).
 * Sem datas, vale o mês corrente; situacao aceita PENDENTE, PARCIAL ou ATENDIDO.
 */
public record ConsultarAnalisePedidosRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        String busca,
        String situacao) {

    public ConsultaAnalisePedidos toDomain() {
        YearMonth mesAtual = YearMonth.now();
        return new ConsultaAnalisePedidos(
                dataInicio != null ? dataInicio : mesAtual.atDay(1),
                dataFim != null ? dataFim : mesAtual.atEndOfMonth(),
                busca,
                situacaoDominio());
    }

    private Situacao situacaoDominio() {
        if (situacao == null || situacao.isBlank()) {
            return null;
        }
        try {
            return Situacao.valueOf(situacao.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FiltroInvalidoException("Situação inválida: use PENDENTE, PARCIAL ou ATENDIDO");
        }
    }
}
