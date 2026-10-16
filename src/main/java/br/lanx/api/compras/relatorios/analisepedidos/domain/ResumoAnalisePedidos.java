package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Totais do período filtrado (não só da página exibida).
 *
 * @param leadTimeMedioDias média dos itens já entregues; null se nenhum foi
 *                          entregue
 */
public record ResumoAnalisePedidos(
        long totalItens,
        BigDecimal valorTotal,
        long pendentes,
        long parciais,
        long atendidos,
        Long leadTimeMedioDias) {

    public static ResumoAnalisePedidos de(List<IndicadoresItem> itens) {
        BigDecimal valor = itens.stream()
                .map(IndicadoresItem::valorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendentes = 0, parciais = 0, atendidos = 0;
        for (IndicadoresItem item : itens) {
            switch (Situacao.de(item.quantidadeAtendida(), item.saldo())) {
                case PENDENTE -> pendentes++;
                case PARCIAL -> parciais++;
                case ATENDIDO -> atendidos++;
            }
        }

        var leadTimes = itens.stream()
                .map(i -> LeadTime.emDias(i.dataEmissaoPc(), i.dataEntregaPc()))
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .summaryStatistics();
        Long leadTimeMedio = leadTimes.getCount() == 0 ? null : Math.round(leadTimes.getAverage());

        return new ResumoAnalisePedidos(itens.size(), valor, pendentes, parciais, atendidos, leadTimeMedio);
    }
}
