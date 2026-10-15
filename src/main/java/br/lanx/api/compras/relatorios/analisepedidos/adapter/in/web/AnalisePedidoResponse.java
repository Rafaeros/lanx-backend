package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnalisePedidoResponse(
        String requisicaoDeCompra,
        String solicitante,
        LocalDate dataEmissaoRc,
        LocalDate dataPrevisaoEntregaRc,
        String pedidoDeCompra,
        String comprador,
        LocalDate dataEmissaoPc,
        LocalDate dataPrevisaoEntregaPc,
        LocalDate dataEntregaPc,
        String fornecedor,
        String pais,
        String codigoItem,
        String descricaoItem,
        String unidadeMedida,
        BigDecimal quantidadeSolicitada,
        BigDecimal quantidadeAtendida,
        BigDecimal saldo,
        Long leadTimeDias,
        boolean atendimentoParcial,
        String local,
        BigDecimal valorUnitario,
        BigDecimal valorFrete,
        BigDecimal ipi,
        BigDecimal valorTotal,
        String condicaoPagamento,
        String ncm,
        String recebimento,
        String notaFiscal,
        Integer quantidadeRecebimentos,
        String rateio,
        String referencia,
        String invoice,
        String responsavelCompras,
        String observacao) {
}