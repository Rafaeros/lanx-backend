package br.lanx.api.producao.operacoes.ordensdeproducao.dtos;

import java.util.List;

/** Resultado da importação da planilha de OPs do ERP. */
public record ImportarOrdensResponse(
        int linhasLidas,
        int criadas,
        int atualizadas,
        /** Finalizadas puladas porque a importação foi feita sem incluí-las. */
        int finalizadasIgnoradas,
        int clientesCriados,
        int materiaisCriados,
        List<LinhaRejeitada> rejeitadas) {
}
