package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.time.LocalDate;

public record RequisicaoCompra(
    LocalDate dataEmissao,
    LocalDate dataPrevisaoEntrega
) {}