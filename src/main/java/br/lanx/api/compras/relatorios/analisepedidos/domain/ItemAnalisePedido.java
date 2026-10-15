package br.lanx.api.compras.relatorios.analisepedidos.domain;

import br.lanx.api.shared.domain.Ids;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record ItemAnalisePedido(
        UUID id,
        UUID materialId,
        UUID unidadeMedidaId,
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

    public ItemAnalisePedido(String requisicaoDeCompra, String solicitante, LocalDate dataEmissaoRc,
            LocalDate dataPrevisaoEntregaRc, String pedidoDeCompra, String comprador,
            LocalDate dataEmissaoPc, LocalDate dataPrevisaoEntregaPc, LocalDate dataEntregaPc,
            String fornecedor, String pais, String codigoItem, String descricaoItem,
            String unidadeMedida, BigDecimal quantidadeSolicitada, BigDecimal quantidadeAtendida,
            BigDecimal saldo, String local, BigDecimal valorUnitario, BigDecimal valorFrete,
            BigDecimal ipi, BigDecimal valorTotal, String condicaoPagamento, String ncm,
            String recebimento, String notaFiscal, Integer quantidadeRecebimentos, String rateio,
            String referencia, String invoice, String responsavelCompras, String observacao) {
        this(Ids.newId(), null, null, requisicaoDeCompra, solicitante, dataEmissaoRc, dataPrevisaoEntregaRc,
                pedidoDeCompra, comprador, dataEmissaoPc, dataPrevisaoEntregaPc, dataEntregaPc, fornecedor, pais,
                codigoItem, descricaoItem, unidadeMedida, quantidadeSolicitada, quantidadeAtendida, saldo, local,
                valorUnitario, valorFrete, ipi, valorTotal, condicaoPagamento, ncm, recebimento, notaFiscal,
                quantidadeRecebimentos, rateio, referencia, invoice, responsavelCompras, observacao);
    }

    public ItemAnalisePedido withMaterialId(UUID materialId) {
        return new ItemAnalisePedido(
                this.id, materialId, this.unidadeMedidaId,
                this.requisicaoDeCompra, this.solicitante, this.dataEmissaoRc, this.dataPrevisaoEntregaRc,
                this.pedidoDeCompra, this.comprador, this.dataEmissaoPc, this.dataPrevisaoEntregaPc, this.dataEntregaPc,
                this.fornecedor, this.pais, this.codigoItem, this.descricaoItem, this.unidadeMedida,
                this.quantidadeSolicitada, this.quantidadeAtendida, this.saldo, this.local,
                this.valorUnitario, this.valorFrete, this.ipi, this.valorTotal, this.condicaoPagamento, this.ncm,
                this.recebimento, this.notaFiscal, this.quantidadeRecebimentos, this.rateio, this.referencia,
                this.invoice, this.responsavelCompras, this.observacao);
    }

    public ItemAnalisePedido withUnidadeMedidaId(UUID unidadeMedidaId) {
        return new ItemAnalisePedido(
                this.id, this.materialId, unidadeMedidaId,
                this.requisicaoDeCompra, this.solicitante, this.dataEmissaoRc, this.dataPrevisaoEntregaRc,
                this.pedidoDeCompra, this.comprador, this.dataEmissaoPc, this.dataPrevisaoEntregaPc, this.dataEntregaPc,
                this.fornecedor, this.pais, this.codigoItem, this.descricaoItem, this.unidadeMedida,
                this.quantidadeSolicitada, this.quantidadeAtendida, this.saldo, this.local,
                this.valorUnitario, this.valorFrete, this.ipi, this.valorTotal, this.condicaoPagamento, this.ncm,
                this.recebimento, this.notaFiscal, this.quantidadeRecebimentos, this.rateio, this.referencia,
                this.invoice, this.responsavelCompras, this.observacao);
    }
}