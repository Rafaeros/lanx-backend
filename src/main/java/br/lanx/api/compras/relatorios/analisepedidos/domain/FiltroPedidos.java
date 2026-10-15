package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.time.LocalDate;

public record FiltroPedidos(
        LocalDate dataInicio,
        LocalDate dataFim) {
}
