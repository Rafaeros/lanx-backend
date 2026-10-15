package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.time.LocalDate;

public record PedidoCompra(
    LocalDate dataEmissao,
    LocalDate dataPrevisaoEntrega,
    LocalDate dataEntrega
) {
    public Long leadTimeDias() {
        if (dataEmissao == null || dataEntrega == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(dataEmissao, dataEntrega);
    }
}