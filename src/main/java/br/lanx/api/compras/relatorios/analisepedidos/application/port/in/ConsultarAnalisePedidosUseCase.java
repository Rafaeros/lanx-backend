package br.lanx.api.compras.relatorios.analisepedidos.application.port.in;

import java.util.List;

import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

public interface ConsultarAnalisePedidosUseCase {
    List<ItemAnalisePedido> consultar(FiltroPedidos filtro);
}
