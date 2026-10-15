package br.lanx.api.compras.relatorios.analisepedidos.application.port.out;

import java.util.List;

import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

public interface ConsultarAnalisePedidosPort {
    List<ItemAnalisePedido> buscarPorPeriodo(FiltroPedidos filtro);
}
