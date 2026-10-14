package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawItemPedido (
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
    BigDecimal  quantidadeSolicitada,
    String quantidadeAtendida,
    BigDecimal saldo,
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
    String responsavelCompras
    String observação,
){}
