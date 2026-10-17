package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AnalisePedidoResponse(
        UUID id,
        String requisicaoDeCompra,
        String solicitante,
        LocalDate dataEmissaoRc,
        LocalDate dataPrevisaoEntregaRc,
        String pedidoDeCompra,
        String comprador,
        LocalDate dataEmissaoPc,
        LocalDate dataPrevisaoEntregaPc,
        LocalDate dataEntregaPc,
        /** Null quando o item veio do ERP sem fornecedor. */
        FornecedorResumo fornecedor,
        MaterialResumo material,
        UnidadeMedidaResumo unidadeMedida,
        BigDecimal quantidadeSolicitada,
        BigDecimal quantidadeAtendida,
        BigDecimal saldo,
        Long leadTimeDias,
        boolean atendimentoParcial,
        /** PENDENTE, PARCIAL ou ATENDIDO (domain.Situacao). */
        String situacao,
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

    public record FornecedorResumo(UUID id, String nome, String pais) {
    }

    public record MaterialResumo(UUID id, String codigo, String descricao) {
    }

    public record UnidadeMedidaResumo(UUID id, String sigla) {
    }
}
