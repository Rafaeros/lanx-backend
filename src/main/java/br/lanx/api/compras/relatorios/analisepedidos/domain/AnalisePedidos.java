package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.util.List;
import java.util.Objects;

public record AnalisePedidos(
        List<ItemAnalisePedido> itens) {
    public AnalisePedidos {
        Objects.requireNonNull(itens, "Itens não podem ser nulos");
        itens = List.copyOf(itens);
    }

    public int totalItens() {
        return itens.size();
    }
}
