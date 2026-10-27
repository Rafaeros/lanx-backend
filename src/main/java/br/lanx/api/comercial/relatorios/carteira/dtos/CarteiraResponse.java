package br.lanx.api.comercial.relatorios.carteira.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Carteira do período, já ordenada pela previsão. {@code consultadoEm} é quando
 * os dados saíram do ERP (pode ser de uma consulta recente, em cache).
 */
public record CarteiraResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        Instant consultadoEm,
        Resumo resumo,
        List<Item> itens) {

    public record Resumo(int itens, BigDecimal valorTotal, int atrasados, int comMaterialPendente) {
    }

    /** {@code atrasado}: previsão de entrega anterior a hoje. */
    public record Item(
            Integer negociacao,
            String pedidoCliente,
            String ordem,
            String tipoServico,
            String cliente,
            String codigo,
            String descricao,
            LocalDate previsao,
            BigDecimal pendente,
            BigDecimal valorTotal,
            String etapaAtual,
            boolean atrasado,
            List<MaterialPendente> materiaisPendentes) {
    }

    public record MaterialPendente(
            String codigo,
            String descricao,
            BigDecimal quantidadePendente,
            String unidadeMedida,
            LocalDate previsao) {
    }
}
