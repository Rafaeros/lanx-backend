package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.util.UUID;

/** Rateio que aparece nos itens importados, para o filtro dos relatórios. */
public record RateioFiltro(UUID id, String nome) {
}
