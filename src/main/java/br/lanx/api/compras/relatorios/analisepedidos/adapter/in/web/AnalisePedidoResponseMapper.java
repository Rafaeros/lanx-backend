package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

public class AnalisePedidoResponseMapper {

    private AnalisePedidoResponseMapper() {
    }

    public static AnalisePedidoResponse from(ItemAnalisePedido item) {
        return new AnalisePedidoResponse(
                item.requisicaoDeCompra(),
                item.solicitante(),
                item.dataEmissaoRc(),
                item.dataPrevisaoEntregaRc(),
                item.pedidoDeCompra(),
                item.comprador(),
                item.dataEmissaoPc(),
                item.dataPrevisaoEntregaPc(),
                item.dataEntregaPc(),
                item.fornecedor(),
                item.pais(),
                item.codigoItem(),
                item.descricaoItem(),
                item.unidadeMedida(),
                item.quantidadeSolicitada(),
                item.quantidadeAtendida(),
                item.saldo(),
                null, // leadTimeDias: calcular se quiser
                false, // atendimentoParcial: calcular se quiser
                item.local(),
                item.valorUnitario(),
                item.valorFrete(),
                item.ipi(),
                item.valorTotal(),
                item.condicaoPagamento(),
                item.ncm(),
                item.recebimento(),
                item.notaFiscal(),
                item.quantidadeRecebimentos(),
                item.rateio(),
                item.referencia(),
                item.invoice(),
                item.responsavelCompras(),
                item.observacao());
    }
}