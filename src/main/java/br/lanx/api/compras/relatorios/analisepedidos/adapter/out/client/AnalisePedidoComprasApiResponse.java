package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalisePedidoComprasApiResponse(
        boolean success,
        List<RawItemPedido> data) {
}
