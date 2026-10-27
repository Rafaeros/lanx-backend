package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.compras.relatorios.analisepedidos.domain.Situacao;

public class AnalisePedidoResponseMapper {

    private AnalisePedidoResponseMapper() {
    }

    public static AnalisePedidoResponse from(ItemAnalisePedido item) {
        return new AnalisePedidoResponse(
                item.id(),
                item.requisicaoDeCompra(),
                item.solicitante(),
                item.dataEmissaoRc(),
                item.dataPrevisaoEntregaRc(),
                item.pedidoDeCompra(),
                item.compradorId() == null ? null
                        : new AnalisePedidoResponse.CompradorResumo(item.compradorId(), item.comprador()),
                item.dataEmissaoPc(),
                item.dataPrevisaoEntregaPc(),
                item.dataEntregaPc(),
                item.fornecedorId() == null ? null
                        : new AnalisePedidoResponse.FornecedorResumo(item.fornecedorId(), item.fornecedor(),
                                item.pais()),
                new AnalisePedidoResponse.MaterialResumo(item.materialId(), item.codigoItem(), item.descricaoItem()),
                new AnalisePedidoResponse.UnidadeMedidaResumo(item.unidadeMedidaId(), item.unidadeMedida()),
                item.quantidadeSolicitada(),
                item.quantidadeAtendida(),
                item.saldo(),
                item.leadTimeDias(),
                item.situacao() == Situacao.PARCIAL,
                item.situacao().name(),
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
                item.rateioId() == null ? null
                        : new AnalisePedidoResponse.RateioResumo(item.rateioId(), item.rateio()),
                item.referencia(),
                item.invoice(),
                item.responsavelCompras(),
                item.observacao());
    }
}