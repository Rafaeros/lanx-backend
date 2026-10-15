package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawItemPedido(
    String requisicaoDeCompra,
    String solicitante,
    String dataEmissaoRc,
    String dataPrevisaoEntregaRc,
    String pedidoDeCompra,
    String comprador,
    String dataEmissaoPc,
    String dataPrevisaoEntregaPc,
    String dataEntregaPc,
    String fornecedor,
    String pais,
    String codigoItem,
    String descricaoItem,
    String unidadeMedida,
    String quantidadeSolicitada,
    String quantidadeAtendida,
    String saldo,
    String local,
    String valorUnitario,
    String valorFrete,
    String ipi,
    String valorTotal,
    String condicaoPagamento,
    String ncm,
    String recebimento,
    String notaFiscal,
    Integer quantidadeRecebimentos,
    String rateio,
    String referencia,
    String invoice,
    String responsavelCompras,
    String observacao
) {}