package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import java.time.LocalDate;
import java.util.List;

public interface AnalisePedidoClient {
    List<RawItemPedido> buscar(LocalDate dataInicio, LocalDate dataFim);
}