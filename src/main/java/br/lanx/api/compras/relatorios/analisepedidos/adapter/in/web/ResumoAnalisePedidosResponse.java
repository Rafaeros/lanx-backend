package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import java.math.BigDecimal;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ResumoAnalisePedidos;

public record ResumoAnalisePedidosResponse(
        long totalItens,
        BigDecimal valorTotal,
        long pendentes,
        long parciais,
        long atendidos,
        Long leadTimeMedioDias) {

    static ResumoAnalisePedidosResponse from(ResumoAnalisePedidos resumo) {
        return new ResumoAnalisePedidosResponse(resumo.totalItens(), resumo.valorTotal(), resumo.pendentes(),
                resumo.parciais(), resumo.atendidos(), resumo.leadTimeMedioDias());
    }
}
