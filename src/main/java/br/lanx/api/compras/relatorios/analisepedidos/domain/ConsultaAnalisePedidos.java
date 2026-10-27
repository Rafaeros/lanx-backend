package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Filtros da consulta da análise de pedidos.
 *
 * @param dataInicio início do período (data de emissão do PC), obrigatório
 * @param dataFim    fim do período, obrigatório e não anterior ao início
 * @param busca      texto livre em pedido, RC, fornecedor, código e descrição do
 *                   item; vazio = sem filtro
 * @param situacao   situação de atendimento; null = todas
 * @param rateioIds  ids dos rateios (cadastros.rateios); vazio = todos
 */
public record ConsultaAnalisePedidos(LocalDate dataInicio, LocalDate dataFim, String busca, Situacao situacao,
        Set<UUID> rateioIds) {

    public ConsultaAnalisePedidos(LocalDate dataInicio, LocalDate dataFim, String busca, Situacao situacao) {
        this(dataInicio, dataFim, busca, situacao, Set.of());
    }

    public ConsultaAnalisePedidos {
        if (dataInicio == null || dataFim == null) {
            throw new FiltroInvalidoException("Informe a data inicial e a data final");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new FiltroInvalidoException("A data inicial deve ser anterior ou igual à data final");
        }
        busca = busca == null ? "" : busca.trim();
        rateioIds = rateioIds == null ? Set.of() : Set.copyOf(rateioIds);
    }

    public boolean temBusca() {
        return !busca.isEmpty();
    }

    public boolean filtraRateios() {
        return !rateioIds.isEmpty();
    }
}
