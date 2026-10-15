package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.math.BigDecimal;

public record Financeiro(
    BigDecimal valorUnitario,
    BigDecimal valorFrete,
    BigDecimal ipi,
    BigDecimal valorTotal,
    String condicaoPagamento,
    String ncm
) {}