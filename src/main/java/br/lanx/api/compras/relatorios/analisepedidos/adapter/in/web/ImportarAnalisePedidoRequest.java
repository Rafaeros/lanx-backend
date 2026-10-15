package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ImportarAnalisePedidoRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
    public FiltroPedidos toDomain() {
        return new FiltroPedidos(dataInicio, dataFim);
    }
}