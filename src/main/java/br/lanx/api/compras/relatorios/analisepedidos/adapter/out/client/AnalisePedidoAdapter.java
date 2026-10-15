package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ObterPedidosExternosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

@Component
public class AnalisePedidoAdapter implements ObterPedidosExternosPort {

    private final AnalisePedidoClient client;
    private final RawItemPedidoMapper mapper;

    public AnalisePedidoAdapter(AnalisePedidoClient client, RawItemPedidoMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<ItemAnalisePedido> obterPedidos(FiltroPedidos filtro) {
        return client.buscar(filtro.dataInicio(), filtro.dataFim()).stream().map(mapper::toDomain).toList();
    }

}
